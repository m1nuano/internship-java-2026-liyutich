package com.demo.senla.dao.specification;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.entity.Employee;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecificationDaoTests extends DaoTest {

    @Autowired private EmployeeSpecificationDao employeeSpecificationDao;
    @Autowired private TravelRequestSpecificationDao travelRequestSpecificationDao;

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
}
