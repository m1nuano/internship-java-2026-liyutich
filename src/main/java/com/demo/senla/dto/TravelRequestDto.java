package com.demo.senla.dto;

import com.demo.senla.entity.TravelRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TravelRequestDto {
    private Long id;
    private Long employeeId;
    private Long departmentId;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;
    private TravelRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal estimatedCost;
}
