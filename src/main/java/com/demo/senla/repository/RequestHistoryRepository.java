package com.demo.senla.repository;

import com.demo.senla.entity.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
}
