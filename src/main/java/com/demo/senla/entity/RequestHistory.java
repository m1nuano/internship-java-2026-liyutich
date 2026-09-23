package com.demo.senla.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_history")
@Getter
@Setter
@NoArgsConstructor
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_request_id")
    private TravelRequest travelRequest;

    @Enumerated(EnumType.STRING)
    private TravelRequestStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private TravelRequestStatus newStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    private String comment;

    @PrePersist
    void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
