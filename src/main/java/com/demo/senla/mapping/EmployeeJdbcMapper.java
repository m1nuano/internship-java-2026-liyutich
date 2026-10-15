package com.demo.senla.mapping;

import com.demo.senla.dto.EmployeeDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class EmployeeJdbcMapper {

    public static final RowMapper<EmployeeDto> ROW_MAPPER = (rs, rowNum) -> toDto(rs);

    private EmployeeJdbcMapper() {
    }

    public static EmployeeDto toDto(ResultSet rs) throws SQLException {
        long departmentId = rs.getLong("department_id");
        Long nullableDepartmentId = rs.wasNull() ? null : departmentId;
        Date hireDate = rs.getDate("hire_date");
        return new EmployeeDto(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("position"),
                nullableDepartmentId,
                hireDate == null ? null : hireDate.toLocalDate()
        );
    }
}
