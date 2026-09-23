package com.demo.senla.service;

import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.entity.Department;
import com.demo.senla.entity.Employee;
import com.demo.senla.repository.DepartmentRepository;
import com.demo.senla.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDto> findAll() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDto> result = new ArrayList<>();

        for (Employee employee : employees) {
            result.add(toDto(employee));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public EmployeeDto findById(Long id) {
        return toDto(findEntity(id));
    }

    public EmployeeDto create(EmployeeDto dto) {
        Employee employee = new Employee();
        fillEmployee(employee, dto);

        Employee savedEmployee = employeeRepository.save(employee);
        return toDto(savedEmployee);
    }

    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee employee = findEntity(id);
        fillEmployee(employee, dto);

        Employee savedEmployee = employeeRepository.save(employee);
        return toDto(savedEmployee);
    }

    public void delete(Long id) {
        Employee employee = findEntity(id);
        employeeRepository.delete(employee);
    }

    private Employee findEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    }

    private void fillEmployee(Employee employee, EmployeeDto dto) {
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPosition(dto.getPosition());
        employee.setHireDate(dto.getHireDate());

        if (dto.getDepartmentId() == null) {
            employee.setDepartment(null);
        } else {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Department not found: " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }
    }

    private EmployeeDto toDto(Employee employee) {
        Long departmentId = employee.getDepartment() == null
                ? null
                : employee.getDepartment().getId();
        return new EmployeeDto(
                employee.getId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getPosition(),
                departmentId,
                employee.getHireDate()
        );
    }
}
