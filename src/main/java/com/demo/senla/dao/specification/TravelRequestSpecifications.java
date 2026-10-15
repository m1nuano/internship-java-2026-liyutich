package com.demo.senla.dao.specification;

import com.demo.senla.entity.Department_;
import com.demo.senla.entity.Employee_;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.entity.TravelRequest_;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public final class TravelRequestSpecifications {
    private TravelRequestSpecifications() { }

    public static Specification<TravelRequest> statusEquals(TravelRequestStatus status) {
        return (root, query, builder) -> status == null ? null
                : builder.equal(root.get(TravelRequest_.status), status);
    }

    public static Specification<TravelRequest> startFrom(LocalDate date) {
        return (root, query, builder) -> date == null ? null
                : builder.greaterThanOrEqualTo(root.get(TravelRequest_.startDate), date);
    }

    public static Specification<TravelRequest> endTo(LocalDate date) {
        return (root, query, builder) -> date == null ? null
                : builder.lessThanOrEqualTo(root.get(TravelRequest_.endDate), date);
    }

    public static Specification<TravelRequest> destinationContains(String destination) {
        return (root, query, builder) -> destination == null || destination.isBlank() ? null
                : builder.like(builder.lower(root.get(TravelRequest_.destination)),
                        "%" + destination.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<TravelRequest> departmentIdEquals(Long departmentId) {
        return (root, query, builder) -> departmentId == null ? null
                : builder.equal(root.get(TravelRequest_.department).get(Department_.id), departmentId);
    }

    public static Specification<TravelRequest> employeeIdEquals(Long employeeId) {
        return (root, query, builder) -> employeeId == null ? null
                : builder.equal(root.get(TravelRequest_.employee).get(Employee_.id), employeeId);
    }

    public static Specification<TravelRequest> minCost(BigDecimal cost) {
        return (root, query, builder) -> cost == null ? null
                : builder.greaterThanOrEqualTo(root.get(TravelRequest_.estimatedCost), cost);
    }

    public static Specification<TravelRequest> maxCost(BigDecimal cost) {
        return (root, query, builder) -> cost == null ? null
                : builder.lessThanOrEqualTo(root.get(TravelRequest_.estimatedCost), cost);
    }
}
