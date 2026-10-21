package com.demo.senla.dao.qbe;

import com.demo.senla.entity.Employee;
import com.demo.senla.entity.Employee_;
import com.demo.senla.repository.EmployeeRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeQbeDao {

    private final EmployeeRepository employeeRepository;

    public EmployeeQbeDao(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> findByPositionContaining(String positionPart, Pageable pageable) {
        Employee probe = new Employee();
        probe.setPosition(positionPart);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher(Employee_.POSITION, match -> match.contains().ignoreCase());
        return employeeRepository.findAll(Example.of(probe, matcher), pageable);
    }
}
