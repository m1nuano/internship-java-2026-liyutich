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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "travel_requests")
@Getter
@Setter
@NoArgsConstructor
public class TravelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(min = 10, max = 500)
    @Column(length = 500)
    private String purpose;

    @Enumerated(EnumType.STRING)
    private TravelRequestStatus status = TravelRequestStatus.DRAFT;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @DecimalMin(value = "0.0")
    private BigDecimal estimatedCost;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TravelRequest that = (TravelRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(employee, that.employee) && Objects.equals(department, that.department) && Objects.equals(destination, that.destination) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(purpose, that.purpose) && status == that.status && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(estimatedCost, that.estimatedCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employee, department, destination, startDate, endDate, purpose, status, createdAt, updatedAt, estimatedCost);
    }

    @Override
    public String toString() {
        return "TravelRequest{" +
                "id=" + id +
                ", employee=" + employee +
                ", department=" + department +
                ", destination='" + destination + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", purpose='" + purpose + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", estimatedCost=" + estimatedCost +
                '}';
    }
}
