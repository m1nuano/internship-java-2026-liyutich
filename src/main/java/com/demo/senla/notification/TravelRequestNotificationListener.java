package com.demo.senla.notification;

import com.demo.senla.metrics.TravelRequestStatusChangedEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification-service", name = "enabled", havingValue = "true")
public class TravelRequestNotificationListener {

    private final NotificationClient notificationClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(TravelRequestStatusChangedEvent event) {
        try {
            notificationClient.send(new StatusChangeNotification(event.requestId(), event.oldStatus(), event.newStatus()));
        } catch (FeignException exception) {
            log.warn("Could not send status notification for travel request {}: {}",
                    event.requestId(), exception.getMessage());
        }
    }
}
