package com.demo.senla.dao.specification;

import com.demo.senla.entity.Employee;
import com.demo.senla.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class EmployeeSpecificationDao {
    private final EmployeeRepository employeeRepository;

    public EmployeeSpecificationDao(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> search(String name, String position, Pageable pageable) {
        return search(name, position, null, null, null, pageable);
    }

    public Page<Employee> search(String name, String position, Long departmentId,
                                 LocalDate hireFrom, LocalDate hireTo, Pageable pageable) {
        Specification<Employee> spec = Specification.where(EmployeeSpecifications.fullNameContains(name))
                .and(EmployeeSpecifications.positionContains(position))
                .and(EmployeeSpecifications.departmentIdEquals(departmentId))
                .and(EmployeeSpecifications.hireDateFrom(hireFrom))
                .and(EmployeeSpecifications.hireDateTo(hireTo));
        return employeeRepository.findAll(spec, pageable);
    }
}
