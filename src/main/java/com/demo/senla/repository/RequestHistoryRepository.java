package com.demo.senla.repository;

import com.demo.senla.entity.RequestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
    Page<RequestHistory> findByTravelRequestId(Long travelRequestId, Pageable pageable);
}
