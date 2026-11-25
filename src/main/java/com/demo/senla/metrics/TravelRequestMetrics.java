package com.demo.senla.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TravelRequestMetrics {

    private final Counter createdRequests;
    private final Counter statusChanges;

    public TravelRequestMetrics(MeterRegistry registry) {
        createdRequests = Counter.builder("travel.requests.created")
                .description("Number of committed travel request creations")
                .register(registry);
        statusChanges = Counter.builder("travel.requests.status.changed")
                .description("Number of committed travel request status changes")
                .register(registry);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(TravelRequestCreatedEvent event) {
        createdRequests.increment();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(TravelRequestStatusChangedEvent event) {
        statusChanges.increment();
    }
}
