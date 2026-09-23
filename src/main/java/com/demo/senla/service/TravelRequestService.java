package com.demo.senla.service;

import com.demo.senla.dto.TravelRequestDto;
import com.demo.senla.entity.Department;
import com.demo.senla.entity.Employee;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.repository.DepartmentRepository;
import com.demo.senla.repository.EmployeeRepository;
import com.demo.senla.repository.TravelRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelRequestService {

    private final TravelRequestRepository travelRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<TravelRequestDto> findAll() {
        List<TravelRequest> requests = travelRequestRepository.findAll();
        List<TravelRequestDto> result = new ArrayList<>();

        for (TravelRequest request : requests) {
            result.add(toDto(request));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public TravelRequestDto findById(Long id) {
        return toDto(findEntity(id));
    }

    public TravelRequestDto create(TravelRequestDto dto) {
        TravelRequest request = new TravelRequest();
        fillTravelRequest(request, dto);

        TravelRequest savedRequest = travelRequestRepository.save(request);
        return toDto(savedRequest);
    }

    public TravelRequestDto update(Long id, TravelRequestDto dto) {
        TravelRequest request = findEntity(id);
        fillTravelRequest(request, dto);

        TravelRequest savedRequest = travelRequestRepository.save(request);
        return toDto(savedRequest);
    }

    public void delete(Long id) {
        TravelRequest request = findEntity(id);
        travelRequestRepository.delete(request);
    }

    private TravelRequest findEntity(Long id) {
        return travelRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Travel request not found: " + id));
    }

    private void fillTravelRequest(TravelRequest request, TravelRequestDto dto) {
        request.setDestination(dto.getDestination());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setPurpose(dto.getPurpose());
        request.setStatus(dto.getStatus());
        request.setEstimatedCost(dto.getEstimatedCost());

        if (dto.getEmployeeId() == null) {
            request.setEmployee(null);
        } else {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found: " + dto.getEmployeeId()));
            request.setEmployee(employee);
        }

        if (dto.getDepartmentId() == null) {
            request.setDepartment(null);
        } else {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Department not found: " + dto.getDepartmentId()));
            request.setDepartment(department);
        }
    }

    private TravelRequestDto toDto(TravelRequest request) {
        Long employeeId = request.getEmployee() == null ? null : request.getEmployee().getId();
        Long departmentId = request.getDepartment() == null ? null : request.getDepartment().getId();
        return new TravelRequestDto(
                request.getId(),
                employeeId,
                departmentId,
                request.getDestination(),
                request.getStartDate(),
                request.getEndDate(),
                request.getPurpose(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getEstimatedCost()
        );
    }
}
