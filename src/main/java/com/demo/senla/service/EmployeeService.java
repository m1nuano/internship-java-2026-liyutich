package com.demo.senla.service;

import com.demo.senla.dao.jdbc.EmployeeJdbcDao;
import com.demo.senla.dao.jdbctemplate.EmployeeJdbcTemplateDao;
import com.demo.senla.dao.qbe.EmployeeQbeDao;
import com.demo.senla.dao.specification.EmployeeSpecificationDao;
import com.demo.senla.dao.springdata.EmployeeSpringDataDao;
import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.dto.EmployeeJdbcCreateRequest;
import com.demo.senla.entity.Department;
import com.demo.senla.entity.Employee;
import com.demo.senla.mapping.EmployeeMapper;
import com.demo.senla.repository.DepartmentRepository;
import com.demo.senla.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeSpecificationDao employeeSpecificationDao;
    private final EmployeeJdbcDao employeeJdbcDao;
    private final EmployeeJdbcTemplateDao employeeJdbcTemplateDao;
    private final EmployeeQbeDao employeeQbeDao;
    private final EmployeeSpringDataDao employeeSpringDataDao;

    @Transactional(readOnly = true)
    public List<EmployeeDto> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> search(String name, String position, Long departmentId,
                                    LocalDate hireFrom, LocalDate hireTo, Pageable pageable) {
        return employeeSpecificationDao.search(name, position, departmentId, hireFrom, hireTo, pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto findById(Long id) {
        return employeeMapper.toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public EmployeeDto findByIdJdbc(Long id) {
        return employeeJdbcDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void transferToDepartmentJdbc(Long employeeId, Long departmentId) {
        int updatedEmployees = employeeJdbcDao.transferToDepartment(employeeId, departmentId);
        if (updatedEmployees == 0) {
            throw new EntityNotFoundException("Employee not found: " + employeeId);
        }
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> findByDepartmentJdbcTemplate(Long departmentId, Pageable pageable) {
        return employeeJdbcTemplateDao.findByDepartmentId(departmentId, pageable);
    }

    public Long createJdbcTemplate(EmployeeJdbcCreateRequest request) {
        return employeeJdbcTemplateDao.createEmployee(
                request.fullName(), request.email(), request.position(), request.departmentId());
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> findByPositionQbe(String position, Pageable pageable) {
        return employeeQbeDao.findByPositionContaining(position, pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> findPageSpringData(Pageable pageable) {
        return employeeSpringDataDao.findPageWithDepartment(pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> findAllWithDepartment() {
        return employeeRepository.findAllWithDepartment()
                .stream()
                .map(employeeMapper::toDto)
                .toList();
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
