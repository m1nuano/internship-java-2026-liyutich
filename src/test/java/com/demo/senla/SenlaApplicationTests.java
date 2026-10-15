package com.demo.senla;

import com.demo.senla.dao.jdbc.EmployeeJdbcDao;
import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.entity.TravelRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SenlaApplicationTests extends PostgresTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EmployeeJdbcDao employeeJdbcDao;

    @Test
    void contextLoadsAndLiquibaseGeneratesRelatedTestDataIds() {
        EmployeeDto employee = employeeJdbcDao.findById(seedEmployeeId()).orElseThrow();
        assertEquals(seedDepartmentId(), employee.departmentId());
        assertEquals("Alex Morgan", employee.fullName());
        assertEquals(LocalDate.of(2022, 4, 11), employee.hireDate());
        assertEquals(1, jdbcTemplate.queryForObject("""
                select count(*) from request_history h
                join travel_requests tr on tr.id = h.travel_request_id
                join employees e on e.id = tr.employee_id
                where e.email = 'alex.morgan@example.com'
                  and h.comment = 'Submitted for approval'
                """, Integer.class));

        Long departmentId = createDepartment("Sequence test", "SEQ");
        Long employeeId = createEmployee("Sequence Test", "sequence@example.com", departmentId, null);
        assertTrue(departmentId > seedDepartmentId());
        assertTrue(employeeId > seedEmployeeId());
        Long requestId = createRequest(employeeId, departmentId, "Sequence City", TravelRequestStatus.DRAFT);
        Long historyId = jdbcTemplate.queryForObject("""
                insert into request_history (travel_request_id, old_status, new_status, changed_at, comment)
                values (?, 'DRAFT', 'SUBMITTED', current_timestamp, 'Sequence history') returning id
                """, Long.class, requestId);
        assertEquals(requestId, jdbcTemplate.queryForObject(
                "select travel_request_id from request_history where id = ?", Long.class, historyId));
        assertEquals("NO", columnNullable("departments", "code"));
        assertEquals("NO", columnNullable("employees", "email"));
    }

    private Long seedDepartmentId() {
        return jdbcTemplate.queryForObject("select id from departments where code = 'ENG'", Long.class);
    }

    private Long seedEmployeeId() {
        return jdbcTemplate.queryForObject("select id from employees where email = 'alex.morgan@example.com'", Long.class);
    }

    private String columnNullable(String tableName, String columnName) {
        return jdbcTemplate.queryForObject("""
                select is_nullable from information_schema.columns
                where table_schema = 'public' and table_name = ? and column_name = ?
                """, String.class, tableName, columnName);
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

}

