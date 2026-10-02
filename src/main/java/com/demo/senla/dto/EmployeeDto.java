package com.demo.senla.dto;

import java.time.LocalDate;

public record EmployeeDto(
        Long id,
        String fullName,
        String email,
        String position,
        Long departmentId,
        LocalDate hireDate
) {
}
