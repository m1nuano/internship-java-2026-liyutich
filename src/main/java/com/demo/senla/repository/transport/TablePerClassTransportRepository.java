package com.demo.senla.repository.transport;

import com.demo.senla.entity.transport.tableperclass.TransportTablePerClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TablePerClassTransportRepository extends JpaRepository<TransportTablePerClass, Long> {

    Page<TransportTablePerClass> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
