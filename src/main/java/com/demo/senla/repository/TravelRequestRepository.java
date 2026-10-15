package com.demo.senla.repository;

import com.demo.senla.entity.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TravelRequestRepository extends JpaRepository<TravelRequest, Long>,
        JpaSpecificationExecutor<TravelRequest> {
}
