package com.demo.senla.dao.jdbctemplate;

import com.demo.senla.entity.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class EmployeeJdbcTemplateDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Employee> employeeRowMapper = (rs, rowNum) -> {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPosition(rs.getString("position"));
        Date hireDate = rs.getDate("hire_date");
        employee.setHireDate(hireDate == null ? null : hireDate.toLocalDate());
        return employee;
    };

    public EmployeeJdbcTemplateDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Employee> findByDepartmentId(long departmentId) {
        return jdbcTemplate.query("SELECT id, full_name, email, position, hire_date FROM employees WHERE department_id = ?",
                employeeRowMapper, departmentId);
    }

    public Long createEmployee(String fullName, String email, String position, long departmentId) {
        return jdbcTemplate.queryForObject("INSERT INTO employees (full_name, email, position, department_id) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, fullName, email, position, departmentId);
    }
}
