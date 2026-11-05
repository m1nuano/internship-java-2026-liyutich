package com.demo.senla.repository;

import com.demo.senla.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Page<Department> findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
