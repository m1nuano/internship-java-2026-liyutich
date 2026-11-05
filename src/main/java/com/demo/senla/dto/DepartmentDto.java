package com.demo.senla.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentDto(
        Long id,
        @NotNull @Size(min = 1, max = 255) String name,
        @NotNull @Size(min = 3, max = 10) String code
) {
}
