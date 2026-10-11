package com.demo.senla.dao.jdbc;

import com.demo.senla.entity.Employee;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class EmployeeJdbcDao {

    private final DataSource dataSource;

    public EmployeeJdbcDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findById(long employeeId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, full_name, email, position FROM employees WHERE id = ?")) {
            statement.setLong(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(toEmployee(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not find employee", e);
        }
    }

    @Transactional
    public int transferToDepartment(long employeeId, long newDepartmentId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE employees SET department_id = ? WHERE id = ?")) {
            statement.setLong(1, newDepartmentId);
            statement.setLong(2, employeeId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not transfer employee", e);
        }
    }

    private Employee toEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPosition(rs.getString("position"));
        return employee;
    }
}
