package com.demo.senla.dto;

import com.demo.senla.entity.TravelRequestStatus;

import java.time.LocalDateTime;

public record RequestHistoryDto(
        Long id,
        Long travelRequestId,
        TravelRequestStatus oldStatus,
        TravelRequestStatus newStatus,
        LocalDateTime changedAt,
        String comment
) {
}
