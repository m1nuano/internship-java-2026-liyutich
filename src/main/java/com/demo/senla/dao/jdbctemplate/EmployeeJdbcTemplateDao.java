package com.demo.senla.dao.jdbctemplate;

import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.mapping.EmployeeJdbcMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class EmployeeJdbcTemplateDao {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeJdbcTemplateDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> findByDepartmentId(long departmentId, Pageable pageable) {
        String sql = """
                SELECT id, full_name, email, position, department_id, hire_date
                FROM employees WHERE department_id = ? ORDER BY id
                LIMIT ? OFFSET ?
                """;
        long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees WHERE department_id = ?", Long.class, departmentId);
        List<EmployeeDto> employees = jdbcTemplate.query(sql, EmployeeJdbcMapper.ROW_MAPPER,
                departmentId, pageable.getPageSize(), pageable.getOffset());
        return new PageImpl<>(employees, pageable, total);
    }

    public Long createEmployee(String fullName, String email, String position, long departmentId) {
        return jdbcTemplate.queryForObject("INSERT INTO employees (full_name, email, position, department_id) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, fullName, email, position, departmentId);
    }

}
