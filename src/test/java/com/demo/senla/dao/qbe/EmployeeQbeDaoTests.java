package com.demo.senla.dao.qbe;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeQbeDaoTests extends DaoTest {

    @Autowired private EmployeeQbeDao employeeQbeDao;

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
}
