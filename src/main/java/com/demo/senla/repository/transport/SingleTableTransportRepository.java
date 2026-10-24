package com.demo.senla.repository.transport;

import com.demo.senla.entity.transport.singletable.TransportSingleTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SingleTableTransportRepository extends JpaRepository<TransportSingleTable, Long> {

    Page<TransportSingleTable> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

