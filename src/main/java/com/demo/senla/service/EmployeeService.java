package com.demo.senla.service;

import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.entity.Department;
import com.demo.senla.entity.Employee;
import com.demo.senla.mapping.EmployeeMapper;
import com.demo.senla.repository.DepartmentRepository;
import com.demo.senla.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public List<EmployeeDto> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDto findById(Long id) {
        return employeeMapper.toDto(findEntity(id));
    }

    public EmployeeDto create(EmployeeDto dto) {
        Employee employee = employeeMapper.toEntity(dto);
        setDepartment(employee, dto.departmentId());

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee employee = findEntity(id);
        employeeMapper.updateEntity(dto, employee);
        setDepartment(employee, dto.departmentId());

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = findEntity(id);
        employeeRepository.delete(employee);
    }

    private Employee findEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    }

    private void setDepartment(Employee employee, Long departmentId) {
        if (departmentId == null) {
            employee.setDepartment(null);
        } else {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Department not found: " + departmentId));
            employee.setDepartment(department);
        }
    }
}
