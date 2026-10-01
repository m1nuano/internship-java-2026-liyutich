package com.demo.senla.dto;

import com.demo.senla.entity.TravelRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record      TravelRequestDto(
        Long id,
        Long employeeId,
        Long departmentId,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        String purpose,
        TravelRequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal estimatedCost
) {
}
