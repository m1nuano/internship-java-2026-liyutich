package com.demo.senla.dao.springdata;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.entity.Employee;
import com.demo.senla.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeSpringDataDaoTests extends DaoTest {

    @Autowired private EmployeeSpringDataDao employeeSpringDataDao;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private EntityManagerFactory entityManagerFactory;

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
}
