package com.demo.senla.dao.specification;

import com.demo.senla.entity.Employee;
import com.demo.senla.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeSpecificationDao {
    private final EmployeeRepository employeeRepository;

    public EmployeeSpecificationDao(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> search(String name, String position, Pageable pageable) {
        Specification<Employee> spec = Specification.where(EmployeeSpecifications.fullNameContains(name))
                .and(EmployeeSpecifications.positionContains(position));
        return employeeRepository.findAll(spec, pageable);
    }
}
