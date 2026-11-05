package com.demo.senla.repository.transport;

import com.demo.senla.entity.transport.joined.TransportJoined;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinedTransportRepository extends JpaRepository<TransportJoined, Long> {

    Page<TransportJoined> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

