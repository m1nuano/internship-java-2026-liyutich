package com.demo.senla.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DepartmentTransferRequest(
        @NotNull @Min(1) Long departmentId
) {
}
