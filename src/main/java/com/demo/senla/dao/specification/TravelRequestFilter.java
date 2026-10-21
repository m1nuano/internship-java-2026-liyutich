package com.demo.senla.dao.specification;

import com.demo.senla.entity.TravelRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class TravelRequestFilter {
    private TravelRequestStatus status;
    private LocalDate startFrom;
    private LocalDate endTo;
    private String destination;
    private Long departmentId;
    private Long employeeId;
    private BigDecimal minCost;
    private BigDecimal maxCost;

    public static TravelRequestFilter empty() {
        return builder().build();
    }
}
