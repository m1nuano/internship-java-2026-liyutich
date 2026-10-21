package com.demo.senla.dao.specification;

import com.demo.senla.entity.TravelRequest;
import com.demo.senla.repository.TravelRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class TravelRequestSpecificationDao {
    private final TravelRequestRepository travelRequestRepository;

    public TravelRequestSpecificationDao(TravelRequestRepository travelRequestRepository) {
        this.travelRequestRepository = travelRequestRepository;
    }

    public Page<TravelRequest> search(TravelRequestFilter filter, Pageable pageable) {
        Specification<TravelRequest> spec = Specification.where(
                TravelRequestSpecifications.statusEquals(filter.getStatus()))
                .and(TravelRequestSpecifications.startFrom(filter.getStartFrom()))
                .and(TravelRequestSpecifications.endTo(filter.getEndTo()))
                .and(TravelRequestSpecifications.destinationContains(filter.getDestination()))
                .and(TravelRequestSpecifications.departmentIdEquals(filter.getDepartmentId()))
                .and(TravelRequestSpecifications.employeeIdEquals(filter.getEmployeeId()))
                .and(TravelRequestSpecifications.minCost(filter.getMinCost()))
                .and(TravelRequestSpecifications.maxCost(filter.getMaxCost()));
        return travelRequestRepository.findAll(spec, pageable);
    }
}
