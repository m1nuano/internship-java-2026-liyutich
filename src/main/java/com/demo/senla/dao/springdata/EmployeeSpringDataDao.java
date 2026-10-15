package com.demo.senla.dao.springdata;

import com.demo.senla.entity.Employee;
import com.demo.senla.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeSpringDataDao {
    private final EmployeeRepository employeeRepository;

    public EmployeeSpringDataDao(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> findPageWithDepartment(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }
}
