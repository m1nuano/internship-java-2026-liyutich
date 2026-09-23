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
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RequestHistory that = (RequestHistory) o;
        return Objects.equals(id, that.id) && Objects.equals(travelRequest, that.travelRequest) && oldStatus == that.oldStatus && newStatus == that.newStatus && Objects.equals(changedAt, that.changedAt) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, travelRequest, oldStatus, newStatus, changedAt, comment);
    }

    @Override
    public String toString() {
        return "RequestHistory{" +
                "id=" + id +
                ", travelRequest=" + travelRequest +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", changedAt=" + changedAt +
                ", comment='" + comment + '\'' +
                '}';
    }
}
