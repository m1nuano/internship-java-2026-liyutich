package com.demo.senla;

import com.demo.senla.dao.criteria.TravelRequestCriteriaDao;
import com.demo.senla.dao.jdbc.EmployeeJdbcDao;
import com.demo.senla.dao.jdbctemplate.EmployeeJdbcTemplateDao;
import com.demo.senla.dao.jpql.TravelRequestJpqlDao;
import com.demo.senla.dao.nativequery.DepartmentStatisticsRepository;
import com.demo.senla.dao.qbe.EmployeeQbeDao;
import com.demo.senla.dao.specification.EmployeeSpecificationDao;
import com.demo.senla.dao.specification.TravelRequestFilter;
import com.demo.senla.dao.specification.TravelRequestSpecificationDao;
import com.demo.senla.dao.springdata.EmployeeSpringDataDao;
import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.entity.Employee;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoTests extends PostgresTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EmployeeJdbcDao employeeJdbcDao;
    @Autowired private EmployeeJdbcTemplateDao employeeJdbcTemplateDao;
    @Autowired private TravelRequestJpqlDao travelRequestJpqlDao;
    @Autowired private DepartmentStatisticsRepository departmentStatisticsRepository;
    @Autowired private EmployeeQbeDao employeeQbeDao;
    @Autowired private EmployeeSpecificationDao employeeSpecificationDao;
    @Autowired private TravelRequestSpecificationDao travelRequestSpecificationDao;
    @Autowired private EmployeeSpringDataDao employeeSpringDataDao;
    @Autowired private TravelRequestCriteriaDao travelRequestCriteriaDao;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @Test
    void jdbcFindsCompleteDtoAndPreservesNullableFields() {
        Long employeeId = createEmployee("Jdbc Test", "jdbc@example.com", seedDepartmentId(), "Analyst",
                LocalDate.of(2024, 3, 1));
        assertEquals(new EmployeeDto(employeeId, "Jdbc Test", "jdbc@example.com", "Analyst",
                seedDepartmentId(), LocalDate.of(2024, 3, 1)), employeeJdbcDao.findById(employeeId).orElseThrow());

        Long nullableId = createEmployee("Nullable Test", "nullable@example.com", null, null);
        assertEquals(new EmployeeDto(nullableId, "Nullable Test", "nullable@example.com", null, null, null),
                employeeJdbcDao.findById(nullableId).orElseThrow());
        assertTrue(employeeJdbcDao.findById(-1L).isEmpty());
    }

    @Test
    void jdbcTransfersEmployeeAndOnlyDraftRequests() {
        Long oldDepartment = createDepartment("Old Department", "OLD");
        Long newDepartment = createDepartment("New Department", "NEW");
        Long employeeId = createEmployee("Transfer Test", "transfer@example.com", oldDepartment, null);
        Long otherEmployee = createEmployee("Other Employee", "other@example.com", oldDepartment, null);
        List<Long> requestIds = new ArrayList<>();
        for (TravelRequestStatus status : TravelRequestStatus.values()) {
            requestIds.add(createRequest(employeeId, oldDepartment, "Berlin", status));
        }
        Long otherDraft = createRequest(otherEmployee, oldDepartment, "Berlin", TravelRequestStatus.DRAFT);

        assertEquals(1, employeeJdbcDao.transferToDepartment(employeeId, newDepartment));
        assertEquals(newDepartment, employeeDepartment(employeeId));
        for (int i = 0; i < TravelRequestStatus.values().length; i++) {
            TravelRequestStatus status = TravelRequestStatus.values()[i];
            Long requestId = requestIds.get(i);
            assertEquals(status == TravelRequestStatus.DRAFT ? newDepartment : oldDepartment,
                    requestDepartment(requestId));
            LocalDateTime updatedAt = jdbcTemplate.queryForObject(
                    "select updated_at from travel_requests where id = ?", LocalDateTime.class, requestId);
            if (status == TravelRequestStatus.DRAFT) {
                assertTrue(updatedAt.isAfter(LocalDateTime.of(2026, 1, 1, 12, 0)));
            } else {
                assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), updatedAt);
            }
        }
        assertEquals(oldDepartment, requestDepartment(otherDraft));
        assertEquals(0, employeeJdbcDao.transferToDepartment(-1, newDepartment));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void jdbcRollsBackEmployeeWhenDraftUpdateFails() {
        Long oldDepartment = createDepartment("Rollback Old", "RBOLD");
        Long newDepartment = createDepartment("Rollback New", "RBNEW");
        Long employeeId = createEmployee("Rollback Test", "rollback@example.com", oldDepartment, null);
        Long requestId = createRequest(employeeId, oldDepartment, "Berlin", TravelRequestStatus.DRAFT);
        try {
            jdbcTemplate.execute("alter table travel_requests add constraint test_reject_draft_transfer "
                    + "check (id <> " + requestId + " or department_id = " + oldDepartment + ")");
            assertThrows(RuntimeException.class, () -> employeeJdbcDao.transferToDepartment(employeeId, newDepartment));
            assertEquals(oldDepartment, employeeDepartment(employeeId));
            assertEquals(oldDepartment, requestDepartment(requestId));
        } finally {
            jdbcTemplate.execute("alter table travel_requests drop constraint if exists test_reject_draft_transfer");
            jdbcTemplate.update("delete from travel_requests where id = ?", requestId);
            jdbcTemplate.update("delete from employees where id = ?", employeeId);
            jdbcTemplate.update("delete from departments where id in (?, ?)", oldDepartment, newDepartment);
        }
    }

    @Test
    void jdbcTemplatePagesCompleteEmployeeDtosAndCreatesEmployee() {
        Long departmentId = createDepartment("Template Department", "TMPL");
        Long first = createEmployee("First", "first@example.com", departmentId, "Analyst", LocalDate.of(2024, 2, 1));
        Long second = createEmployee("Second", "second@example.com", departmentId, null);
        Long third = employeeJdbcTemplateDao.createEmployee("Third", "third@example.com", "Developer", departmentId);
        assertNotNull(third);

        var firstPage = employeeJdbcTemplateDao.findByDepartmentId(departmentId, PageRequest.of(0, 2));
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(List.of(first, second), firstPage.stream().map(EmployeeDto::id).toList());
        assertEquals(new EmployeeDto(first, "First", "first@example.com", "Analyst", departmentId,
                LocalDate.of(2024, 2, 1)), firstPage.getContent().getFirst());
        assertEquals(new EmployeeDto(second, "Second", "second@example.com", null, departmentId, null),
                firstPage.getContent().get(1));
        var secondPage = employeeJdbcTemplateDao.findByDepartmentId(departmentId, PageRequest.of(1, 2));
        assertEquals(3, secondPage.getTotalElements());
        assertEquals(List.of(third), secondPage.stream().map(EmployeeDto::id).toList());
        assertEquals("Developer", secondPage.getContent().getFirst().position());
        assertTrue(employeeJdbcTemplateDao.findByDepartmentId(-1, PageRequest.of(0, 2)).isEmpty());
    }

    @Test
    void jpqlPagesRequestsByDepartmentCode() {
        Long extra = createRequest(seedEmployeeId(), seedDepartmentId(), "Paris", TravelRequestStatus.DRAFT);
        var firstPage = travelRequestJpqlDao.findByDepartmentCode("ENG", PageRequest.of(0, 1));
        assertEquals(2, firstPage.getTotalElements());
        assertEquals("Berlin", firstPage.getContent().getFirst().getDestination());
        var secondPage = travelRequestJpqlDao.findByDepartmentCode("ENG", PageRequest.of(1, 1));
        assertEquals(List.of(extra), secondPage.stream().map(TravelRequest::getId).toList());
        assertTrue(travelRequestJpqlDao.findByDepartmentCode("UNKNOWN", PageRequest.of(0, 1)).isEmpty());
    }

    @Test
    void nativeQueryReturnsDepartmentCountsAndCosts() {
        var engineering = departmentStatisticsRepository.findStatistics().stream()
                .filter(stat -> "Engineering".equals(stat.getDepartmentName())).findFirst().orElseThrow();
        assertEquals(1L, engineering.getRequestCount());
        assertEquals(0, new BigDecimal("1250.00").compareTo(engineering.getTotalEstimatedCost()));

        createRequest(seedEmployeeId(), seedDepartmentId(), "Paris", TravelRequestStatus.DRAFT);
        var updated = departmentStatisticsRepository.findStatistics().stream()
                .filter(stat -> "Engineering".equals(stat.getDepartmentName())).findFirst().orElseThrow();
        assertEquals(2L, updated.getRequestCount());
        assertEquals(0, new BigDecimal("1750.00").compareTo(updated.getTotalEstimatedCost()));
    }

    @Test
    void qbePagesEmployeesByPartOfPositionIgnoringCase() {
        Long departmentId = seedDepartmentId();
        Long first = createEmployee("First Auditor", "auditor1@example.com", departmentId, "Quality Auditor");
        Long second = createEmployee("Second Auditor", "auditor2@example.com", departmentId, "Senior AUDITOR");
        Long third = createEmployee("Third Auditor", "auditor3@example.com", departmentId, "Auditor");
        createEmployee("Other Position", "otherposition@example.com", departmentId, "Analyst");
        var firstPage = employeeQbeDao.findByPositionContaining("aud", PageRequest.of(0, 2, Sort.by("id")));
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(List.of(first, second), firstPage.stream().map(Employee::getId).toList());
        var secondPage = employeeQbeDao.findByPositionContaining("aud", PageRequest.of(1, 2, Sort.by("id")));
        assertEquals(3, secondPage.getTotalElements());
        assertEquals(List.of(third), secondPage.stream().map(Employee::getId).toList());
        assertTrue(employeeQbeDao.findByPositionContaining("not-existing", PageRequest.of(0, 2)).isEmpty());
    }

    @Test
    void employeeSpecificationCombinesTextDepartmentAndHireDateFilters() {
        Long departmentId = createDepartment("Specification Department", "SPEC");
        Long employeeId = createEmployee("Dana Engineer", "dana@example.com", departmentId, "Senior Analyst",
                LocalDate.of(2024, 5, 1));
        createEmployee("Dana Other", "danaother@example.com", seedDepartmentId(), "Senior Analyst",
                LocalDate.of(2021, 1, 1));
        var page = employeeSpecificationDao.search("DANA", "anal", departmentId,
                LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 1), PageRequest.of(0, 10));
        assertEquals(List.of(employeeId), page.stream().map(Employee::getId).toList());
        assertTrue(employeeSpecificationDao.search("Dana", "anal", departmentId,
                LocalDate.of(2025, 1, 1), null, PageRequest.of(0, 10)).isEmpty());
        assertTrue(employeeSpecificationDao.search("Dana", "anal", departmentId,
                null, LocalDate.of(2023, 1, 1), PageRequest.of(0, 10)).isEmpty());
        var originalSearch = employeeSpecificationDao.search("alex", "engineer", PageRequest.of(0, 10));
        assertEquals(List.of(seedEmployeeId()), originalSearch.stream().map(Employee::getId).toList());
        assertEquals(List.of(employeeId), employeeSpecificationDao.search(null, null, departmentId,
                null, null, PageRequest.of(0, 10)).stream().map(Employee::getId).toList());
    }

    @Test
    void travelSpecificationUsesEveryOptionalFilterAndCombinesThem() {
        Long departmentId = createDepartment("Travel Filter Department", "TRFLT");
        Long employeeId = createEmployee("Travel Filter", "travelfilter@example.com", departmentId, null);
        Long requestId = createRequest(employeeId, departmentId, "Oslo office", TravelRequestStatus.DRAFT,
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 15), new BigDecimal("1200.00"));
        Long otherRequest = createRequest(seedEmployeeId(), seedDepartmentId(), "Paris", TravelRequestStatus.APPROVED,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 1), new BigDecimal("3000.00"));
        var pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<TravelRequestFilter> filters = List.of(
                TravelRequestFilter.builder().status(TravelRequestStatus.DRAFT).build(),
                TravelRequestFilter.builder().destination("OSLO").build(),
                TravelRequestFilter.builder().departmentId(departmentId).build(),
                TravelRequestFilter.builder().employeeId(employeeId).build(),
                TravelRequestFilter.builder().startFrom(LocalDate.of(2026, 4, 10)).build(),
                TravelRequestFilter.builder().endTo(LocalDate.of(2026, 4, 15)).build(),
                TravelRequestFilter.builder().minCost(new BigDecimal("1200.00")).build(),
                TravelRequestFilter.builder().maxCost(new BigDecimal("1200.00")).build());
        List<Long> expectedCounts = List.of(1L, 1L, 1L, 1L, 2L, 1L, 3L, 1L);
        for (int i = 0; i < filters.size(); i++) {
            var filteredPage = travelRequestSpecificationDao.search(filters.get(i), pageable);
            assertEquals(expectedCounts.get(i).longValue(), filteredPage.getTotalElements());
            assertTrue(filteredPage.stream()
                    .anyMatch(request -> request.getId().equals(requestId)));
        }

        var filter = TravelRequestFilter.builder().status(TravelRequestStatus.DRAFT)
                .startFrom(LocalDate.of(2026, 4, 10)).endTo(LocalDate.of(2026, 4, 15))
                .destination("OSLO").departmentId(departmentId).employeeId(employeeId)
                .minCost(new BigDecimal("1200.00")).maxCost(new BigDecimal("1200.00")).build();
        assertEquals(List.of(requestId), travelRequestSpecificationDao.search(filter, pageable).stream()
                .map(TravelRequest::getId).toList());
        assertTrue(travelRequestSpecificationDao.search(
                TravelRequestFilter.builder().minCost(new BigDecimal("3000.01")).build(), pageable).isEmpty());
        var minCostPage = travelRequestSpecificationDao.search(
                TravelRequestFilter.builder().minCost(new BigDecimal("1250.00")).build(), pageable);
        assertEquals(2, minCostPage.getTotalElements());
        assertFalse(minCostPage.stream().anyMatch(request -> request.getId().equals(requestId)));
        assertEquals(List.of(requestId), travelRequestSpecificationDao.search(
                TravelRequestFilter.builder().maxCost(new BigDecimal("1200.00")).build(), pageable).stream()
                .map(TravelRequest::getId).toList());
        assertFalse(travelRequestSpecificationDao.search(
                TravelRequestFilter.builder().startFrom(LocalDate.of(2026, 4, 10)).build(), pageable).stream()
                .anyMatch(request -> request.getId().equals(otherRequest)));
        assertEquals(3, travelRequestSpecificationDao.search(TravelRequestFilter.empty(), pageable).getTotalElements());
        var secondPage = travelRequestSpecificationDao.search(TravelRequestFilter.empty(),
                PageRequest.of(1, 2, Sort.by("id")));
        assertEquals(3, secondPage.getTotalElements());
        assertEquals(List.of(otherRequest), secondPage.stream().map(TravelRequest::getId).toList());
    }

    @Test
    void criteriaPagesRequestsAndSupportsEmptyAndPartialFilters() {
        Long first = createRequest(seedEmployeeId(), seedDepartmentId(), "Porto", TravelRequestStatus.APPROVED);
        Long second = createRequest(seedEmployeeId(), seedDepartmentId(), "PORTO West", TravelRequestStatus.APPROVED);
        createRequest(seedEmployeeId(), seedDepartmentId(), "Madrid", TravelRequestStatus.APPROVED);
        var firstPage = travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.APPROVED, "port", PageRequest.of(0, 1));
        assertEquals(2, firstPage.getTotalElements());
        assertEquals(List.of(first), firstPage.stream().map(TravelRequest::getId).toList());
        var secondPage = travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.APPROVED, "port", PageRequest.of(1, 1));
        assertEquals(2, secondPage.getTotalElements());
        assertEquals(List.of(second), secondPage.stream().map(TravelRequest::getId).toList());
        assertEquals(3, travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.APPROVED, null, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(2, travelRequestCriteriaDao.findByStatusAndDestination(
                null, "PORT", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(4, travelRequestCriteriaDao.findByStatusAndDestination(
                null, " ", PageRequest.of(0, 10)).getTotalElements());
        assertTrue(travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.DRAFT, "PORT", PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void springDataPagesEmployeesWithTheirDepartment() {
        Long departmentId = createDepartment("Paged Department", "PAGED");
        Long employeeId = createEmployee("Paged Employee", "paged@example.com", departmentId, null);
        entityManager.flush();
        entityManager.clear();

        var firstPage = employeeSpringDataDao.findPageWithDepartment(PageRequest.of(0, 1, Sort.by("id")));
        assertEquals(2, firstPage.getTotalElements());
        assertEquals("Engineering", firstPage.getContent().getFirst().getDepartment().getName());
        var secondPage = employeeSpringDataDao.findPageWithDepartment(PageRequest.of(1, 1, Sort.by("id")));
        assertEquals(List.of(employeeId), secondPage.stream().map(Employee::getId).toList());
        assertEquals("Paged Department", secondPage.getContent().getFirst().getDepartment().getName());
    }

    @Test
    void entityGraphRemovesNPlusOneQueries() {
        for (int i = 1; i <= 3; i++) {
            Long departmentId = createDepartment("Graph Department " + i, "GRAPH" + i);
            createEmployee("Graph Employee " + i, "graph" + i + "@example.com", departmentId, null);
        }
        entityManager.flush();
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.clear();
        List<Employee> plain = employeeRepository.findAll();
        List<String> plainDepartments = plain.stream().map(employee -> employee.getDepartment().getName()).sorted().toList();
        long plainQueryCount = statistics.getPrepareStatementCount();

        entityManager.clear();
        statistics.clear();
        List<Employee> graph = employeeRepository.findAllWithDepartment();
        List<String> graphDepartments = graph.stream().map(employee -> employee.getDepartment().getName()).sorted().toList();
        long graphQueryCount = statistics.getPrepareStatementCount();
        assertEquals(plainDepartments, graphDepartments);
        assertTrue(plainQueryCount > 1);
        assertEquals(1, graphQueryCount);
    }

    private Long seedDepartmentId() {
        return jdbcTemplate.queryForObject("select id from departments where code = 'ENG'", Long.class);
    }

    private Long seedEmployeeId() {
        return jdbcTemplate.queryForObject("select id from employees where email = 'alex.morgan@example.com'", Long.class);
    }

    private Long createDepartment(String name, String code) {
        return jdbcTemplate.queryForObject("insert into departments (name, code) values (?, ?) returning id",
                Long.class, name, code);
    }

    private Long createEmployee(String name, String email, Long departmentId, String position) {
        return createEmployee(name, email, departmentId, position, null);
    }

    private Long createEmployee(String name, String email, Long departmentId, String position, LocalDate hireDate) {
        return jdbcTemplate.queryForObject("""
                insert into employees (full_name, email, department_id, position, hire_date)
                values (?, ?, ?, ?, ?) returning id
                """, Long.class, name, email, departmentId, position, hireDate);
    }

    private Long createRequest(Long employeeId, Long departmentId, String destination, TravelRequestStatus status) {
        return createRequest(employeeId, departmentId, destination, status, LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 5), new BigDecimal("500.00"));
    }

    private Long createRequest(Long employeeId, Long departmentId, String destination, TravelRequestStatus status,
                               LocalDate startDate, LocalDate endDate, BigDecimal cost) {
        return jdbcTemplate.queryForObject("""
                insert into travel_requests (employee_id, department_id, destination, status,
                    start_date, end_date, estimated_cost, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, timestamp '2026-01-01 12:00:00', timestamp '2026-01-01 12:00:00')
                returning id
                """, Long.class, employeeId, departmentId, destination, status.name(), startDate, endDate, cost);
    }

    private Long employeeDepartment(Long employeeId) {
        return jdbcTemplate.queryForObject("select department_id from employees where id = ?", Long.class, employeeId);
    }

    private Long requestDepartment(Long requestId) {
        return jdbcTemplate.queryForObject("select department_id from travel_requests where id = ?", Long.class, requestId);
    }

}
