package com.demo.senla.dto;

import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.validator.DateRange;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@DateRange
public record TravelRequestDto(
        Long id,
        @Min(1) Long employeeId,
        @Min(1) Long departmentId,
        @NotNull @Size(min = 1, max = 255) String destination,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Size(min = 10, max = 500) String purpose,
        TravelRequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        @DecimalMin("0.0") BigDecimal estimatedCost
) {
}
