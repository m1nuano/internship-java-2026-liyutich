package com.demo.senla;

import com.demo.senla.dao.criteria.TravelRequestCriteriaDao;
import com.demo.senla.dao.jdbc.EmployeeJdbcDao;
import com.demo.senla.dao.jdbctemplate.EmployeeJdbcTemplateDao;
import com.demo.senla.dao.jpql.TravelRequestJpqlDao;
import com.demo.senla.dao.nativequery.DepartmentStatisticsDao;
import com.demo.senla.dao.qbe.EmployeeQbeDao;
import com.demo.senla.dao.specification.EmployeeSpecificationDao;
import com.demo.senla.dao.springdata.EmployeeSpringDataDao;
import com.demo.senla.entity.TravelRequestStatus;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
class SenlaApplicationTests {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("travel_request_system")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired private EmployeeJdbcDao employeeJdbcDao;
    @Autowired private EmployeeJdbcTemplateDao employeeJdbcTemplateDao;
    @Autowired private TravelRequestJpqlDao travelRequestJpqlDao;
    @Autowired private DepartmentStatisticsDao departmentStatisticsDao;
    @Autowired private EmployeeQbeDao employeeQbeDao;
    @Autowired private EmployeeSpecificationDao employeeSpecificationDao;
    @Autowired private EmployeeSpringDataDao employeeSpringDataDao;
    @Autowired private TravelRequestCriteriaDao travelRequestCriteriaDao;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void contextLoadsAndLiquibaseLoadsTestData() {
        Integer seededHistoryRows = jdbcTemplate.queryForObject(
                "select count(*) from request_history where comment = 'Submitted for approval'",
                Integer.class
        );

        assertEquals(1, seededHistoryRows);

        Long generatedDepartmentId = jdbcTemplate.queryForObject(
                "insert into departments (name, code) values ('Sequence test', 'SEQ') returning id",
                Long.class
        );

        assertTrue(generatedDepartmentId > 1001);
    }

    @Test
    void jdbcDaoFindsEmployeeAndTransfersDepartment() {
        assertEquals("Alex Morgan", employeeJdbcDao.findById(1001L).orElseThrow().getFullName());
        assertFalse(employeeJdbcDao.findById(-1L).isPresent());

        Long newDepartmentId = jdbcTemplate.queryForObject(
                "insert into departments (name, code) values ('Test Department', 'TST') returning id", Long.class);
        Long employeeId = createEmployee("Jdbc Test", "jdbc.test@example.com", 1001);
        assertEquals(1, employeeJdbcDao.transferToDepartment(employeeId, newDepartmentId));
        assertEquals(0, employeeJdbcDao.transferToDepartment(-1, newDepartmentId));
        assertEquals(newDepartmentId, jdbcTemplate.queryForObject(
                "select department_id from employees where id = ?", Long.class, employeeId));
    }

    @Test
    void jdbcTemplateDaoFindsAndCreatesEmployees() {
        assertTrue(employeeJdbcTemplateDao.findByDepartmentId(1001).stream()
                .anyMatch(employee -> "Alex Morgan".equals(employee.getFullName())));
        Long employeeId = employeeJdbcTemplateDao.createEmployee(
                "Template Test", "template.test@example.com", "Analyst", 1001);
        assertNotNull(employeeId);
    }

    @Test
    void jpqlDaoPagesRequestsByDepartmentCode() {
        var page = travelRequestJpqlDao.findByDepartmentCode("ENG", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Berlin", page.getContent().getFirst().getDestination());
    }

    @Test
    void nativeQueryDaoReturnsDepartmentStatistics() {
        var engineering = departmentStatisticsDao.findStatistics().stream()
                .filter(stat -> "Engineering".equals(stat.getDepartmentName())).findFirst().orElseThrow();
        assertEquals(1L, engineering.getRequestCount());
        assertEquals(1250.00, engineering.getTotalEstimatedCost().doubleValue());
    }

    @Test
    void qbeFindsEmployeesByPartOfPosition() {
        createEmployee("Qbe Test", "qbe.test@example.com", 1001, "Senior Accountant");
        assertTrue(employeeQbeDao.findByPositionContaining("account").stream()
                .anyMatch(employee -> "Qbe Test".equals(employee.getFullName())));
    }

    @Test
    void specificationSearchesEmployeesWithLikeFilters() {
        var page = employeeSpecificationDao.search("alex", "engineer", PageRequest.of(0, 10));
        assertTrue(page.getContent().stream().anyMatch(employee -> employee.getId().equals(1001L)));
    }

    @Test
    void springDataDaoLoadsDepartmentAndPagesEmployees() {
        var employee = employeeSpringDataDao.findPageWithDepartment(PageRequest.of(0, 10)).getContent()
                .stream().filter(e -> e.getId().equals(1001L)).findFirst().orElseThrow();
        assertEquals("Engineering", employee.getDepartment().getName());
    }

    @Test
    void criteriaDaoFiltersRequestsByStatusAndDestination() {
        var requests = travelRequestCriteriaDao.findByStatusAndDestination(
                TravelRequestStatus.SUBMITTED, "erl");
        assertEquals(1, requests.size());
        assertEquals("Berlin", requests.getFirst().getDestination());
    }

    private Long createEmployee(String name, String email, long departmentId) {
        return createEmployee(name, email, departmentId, null);
    }

    private Long createEmployee(String name, String email, long departmentId, String position) {
        return jdbcTemplate.queryForObject("""
                insert into employees (full_name, email, department_id, position)
                values (?, ?, ?, ?) returning id
                """, Long.class, name, email, departmentId, position);
    }
}
