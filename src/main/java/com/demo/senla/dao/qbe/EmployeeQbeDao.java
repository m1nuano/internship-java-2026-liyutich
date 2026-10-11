package com.demo.senla.dao.qbe;

import com.demo.senla.entity.Employee;
import com.demo.senla.repository.EmployeeRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeQbeDao {

    private final EmployeeRepository employeeRepository;

    public EmployeeQbeDao(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findByPositionContaining(String positionPart) {
        Employee probe = new Employee();
        probe.setPosition(positionPart);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("position", match -> match.contains().ignoreCase());
        return employeeRepository.findAll(Example.of(probe, matcher));
    }
}
