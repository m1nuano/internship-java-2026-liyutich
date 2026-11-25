package com.demo.senla.notification;

import com.demo.senla.entity.TravelRequestStatus;

public record StatusChangeNotification(
        Long travelRequestId,
        TravelRequestStatus oldStatus,
        TravelRequestStatus newStatus
) {
}
