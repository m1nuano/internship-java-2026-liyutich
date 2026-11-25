package com.demo.senla.metrics;

import com.demo.senla.entity.TravelRequestStatus;

public record TravelRequestStatusChangedEvent(
        Long requestId,
        TravelRequestStatus oldStatus,
        TravelRequestStatus newStatus
) {
}
