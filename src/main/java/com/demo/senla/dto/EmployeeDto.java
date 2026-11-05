package com.demo.senla.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeDto(
        Long id,
        @NotNull @Size(min = 1, max = 255) String fullName,
        @NotNull @Email @Size(max = 255) String email,
        @Size(max = 255) String position,
        @Min(1) Long departmentId,
        LocalDate hireDate
) {
}
