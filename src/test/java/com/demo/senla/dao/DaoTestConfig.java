package com.demo.senla.dao;

import com.demo.senla.PostgresTestContainerConfig;
import com.demo.senla.dao.criteria.TravelRequestCriteriaDao;
import com.demo.senla.dao.jdbc.EmployeeJdbcDao;
import com.demo.senla.dao.jdbctemplate.EmployeeJdbcTemplateDao;
import com.demo.senla.dao.jpql.TravelRequestJpqlDao;
import com.demo.senla.dao.nativequery.DepartmentStatisticsRepository;
import com.demo.senla.dao.qbe.EmployeeQbeDao;
import com.demo.senla.dao.specification.EmployeeSpecificationDao;
import com.demo.senla.dao.specification.TravelRequestSpecificationDao;
import com.demo.senla.dao.springdata.EmployeeSpringDataDao;
import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class DaoTestConfig extends PostgresTestContainerConfig {

    @Autowired protected JdbcTemplate jdbcTemplate;
    @Autowired protected TravelRequestCriteriaDao travelRequestCriteriaDao;
    @Autowired protected EmployeeJdbcDao employeeJdbcDao;
    @Autowired protected EmployeeJdbcTemplateDao employeeJdbcTemplateDao;
    @Autowired protected TravelRequestJpqlDao travelRequestJpqlDao;
    @Autowired protected DepartmentStatisticsRepository departmentStatisticsRepository;
    @Autowired protected EmployeeQbeDao employeeQbeDao;
    @Autowired protected EmployeeSpecificationDao employeeSpecificationDao;
    @Autowired protected TravelRequestSpecificationDao travelRequestSpecificationDao;
    @Autowired protected EmployeeSpringDataDao employeeSpringDataDao;
    @Autowired protected EmployeeRepository employeeRepository;
    @Autowired protected EntityManager entityManager;
    @Autowired protected EntityManagerFactory entityManagerFactory;

    protected Long seedDepartmentId() {
        return jdbcTemplate.queryForObject("select id from departments where code = 'ENG'", Long.class);
    }

    protected Long seedEmployeeId() {
        return jdbcTemplate.queryForObject("select id from employees where email = 'alex.morgan@example.com'", Long.class);
    }

    protected Long createDepartment(String name, String code) {
        return jdbcTemplate.queryForObject("insert into departments (name, code) values (?, ?) returning id",
                Long.class, name, code);
    }

    protected Long createEmployee(String name, String email, Long departmentId, String position) {
        return createEmployee(name, email, departmentId, position, null);
    }

    protected Long createEmployee(String name, String email, Long departmentId, String position, LocalDate hireDate) {
        return jdbcTemplate.queryForObject("""
                insert into employees (full_name, email, department_id, position, hire_date)
                values (?, ?, ?, ?, ?) returning id
                """, Long.class, name, email, departmentId, position, hireDate);
    }

    protected Long createRequest(Long employeeId, Long departmentId, String destination, TravelRequestStatus status) {
        return createRequest(employeeId, departmentId, destination, status, LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 5), new BigDecimal("500.00"));
    }

    protected Long createRequest(Long employeeId, Long departmentId, String destination, TravelRequestStatus status,
                               LocalDate startDate, LocalDate endDate, BigDecimal cost) {
        return jdbcTemplate.queryForObject("""
                insert into travel_requests (employee_id, department_id, destination, status,
                    start_date, end_date, estimated_cost, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, timestamp '2026-01-01 12:00:00', timestamp '2026-01-01 12:00:00')
                returning id
                """, Long.class, employeeId, departmentId, destination, status.name(), startDate, endDate, cost);
    }

    protected Long employeeDepartment(Long employeeId) {
        return jdbcTemplate.queryForObject("select department_id from employees where id = ?", Long.class, employeeId);
    }

    protected Long requestDepartment(Long requestId) {
        return jdbcTemplate.queryForObject("select department_id from travel_requests where id = ?", Long.class, requestId);
    }
}
