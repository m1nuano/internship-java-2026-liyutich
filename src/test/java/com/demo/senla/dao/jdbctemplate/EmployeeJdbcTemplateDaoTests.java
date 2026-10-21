package com.demo.senla.dao.jdbctemplate;

import com.demo.senla.dao.DaoTest;
import com.demo.senla.dto.EmployeeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeJdbcTemplateDaoTests extends DaoTest {

    @Autowired private EmployeeJdbcTemplateDao employeeJdbcTemplateDao;

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
}
