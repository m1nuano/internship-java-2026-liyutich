package com.demo.senla.dto;

import com.demo.senla.entity.TravelRequestStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RequestHistoryDto(
        Long id,
        @NotNull @Min(1) Long travelRequestId,
        TravelRequestStatus oldStatus,
        TravelRequestStatus newStatus,
        LocalDateTime changedAt,
        @Size(max = 255) String comment
) {
}
