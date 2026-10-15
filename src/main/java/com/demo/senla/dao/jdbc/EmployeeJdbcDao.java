package com.demo.senla.dao.jdbc;

import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.mapping.EmployeeJdbcMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class EmployeeJdbcDao {

    private final DataSource dataSource;

    public EmployeeJdbcDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDto> findById(long employeeId) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, full_name, email, position, department_id, hire_date FROM employees WHERE id = ?")) {
            statement.setLong(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(EmployeeJdbcMapper.toDto(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not find employee", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Transactional
    public int transferToDepartment(long employeeId, long newDepartmentId) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            int updatedEmployees;
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE employees SET department_id = ? WHERE id = ?")) {
                statement.setLong(1, newDepartmentId);
                statement.setLong(2, employeeId);
                updatedEmployees = statement.executeUpdate();
            }
            if (updatedEmployees == 0) {
                return 0;
            }
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE travel_requests
                    SET department_id = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE employee_id = ? AND status = 'DRAFT'
                    """)) {
                statement.setLong(1, newDepartmentId);
                statement.setLong(2, employeeId);
                statement.executeUpdate();
            }
            return updatedEmployees;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not transfer employee", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
