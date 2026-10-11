package com.demo.senla.repository;

import com.demo.senla.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee>, QueryByExampleExecutor<Employee> {

    @EntityGraph(attributePaths = "department")
    Page<Employee> findAll(Pageable pageable);
}
