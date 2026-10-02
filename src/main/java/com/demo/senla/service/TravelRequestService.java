package com.demo.senla.service;

import com.demo.senla.dto.TravelRequestDto;
import com.demo.senla.entity.Department;
import com.demo.senla.entity.Employee;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.mapping.TravelRequestMapper;
import com.demo.senla.repository.DepartmentRepository;
import com.demo.senla.repository.EmployeeRepository;
import com.demo.senla.repository.TravelRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelRequestService {

    private final TravelRequestRepository travelRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TravelRequestMapper travelRequestMapper;

    @Transactional(readOnly = true)
    public List<TravelRequestDto> findAll() {
        return travelRequestRepository.findAll()
                .stream()
                .map(travelRequestMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TravelRequestDto findById(Long id) {
        return travelRequestMapper.toDto(findEntity(id));
    }

    public TravelRequestDto create(TravelRequestDto dto) {
        TravelRequest request = travelRequestMapper.toEntity(dto);
        setReferences(request, dto.employeeId(), dto.departmentId());

        return travelRequestMapper.toDto(travelRequestRepository.save(request));
    }

    public TravelRequestDto update(Long id, TravelRequestDto dto) {
        TravelRequest request = findEntity(id);
        travelRequestMapper.updateEntity(dto, request);
        setReferences(request, dto.employeeId(), dto.departmentId());

        return travelRequestMapper.toDto(travelRequestRepository.save(request));
    }

    public void delete(Long id) {
        TravelRequest request = findEntity(id);
        travelRequestRepository.delete(request);
    }

    private TravelRequest findEntity(Long id) {
        return travelRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Travel request not found: " + id));
    }

    private void setReferences(TravelRequest request, Long employeeId, Long departmentId) {
        if (employeeId == null) {
            request.setEmployee(null);
        } else {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found: " + employeeId));
            request.setEmployee(employee);
        }

        if (departmentId == null) {
            request.setDepartment(null);
        } else {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Department not found: " + departmentId));
            request.setDepartment(department);
        }
    }
}
