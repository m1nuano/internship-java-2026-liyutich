package com.demo.senla.service;

import com.demo.senla.dao.criteria.TravelRequestCriteriaDao;
import com.demo.senla.dao.jpql.TravelRequestJpqlDao;
import com.demo.senla.dao.specification.TravelRequestFilter;
import com.demo.senla.dao.specification.TravelRequestSpecificationDao;
import com.demo.senla.dto.TravelRequestDto;
import com.demo.senla.entity.Department;
import com.demo.senla.entity.Employee;
import com.demo.senla.entity.RequestHistory;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.mapping.TravelRequestMapper;
import com.demo.senla.repository.DepartmentRepository;
import com.demo.senla.repository.EmployeeRepository;
import com.demo.senla.repository.RequestHistoryRepository;
import com.demo.senla.repository.TravelRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelRequestService {

    private final TravelRequestRepository travelRequestRepository;
    private final RequestHistoryRepository requestHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TravelRequestMapper travelRequestMapper;
    private final TravelRequestSpecificationDao travelRequestSpecificationDao;
    private final TravelRequestJpqlDao travelRequestJpqlDao;
    private final TravelRequestCriteriaDao travelRequestCriteriaDao;

    @Transactional(readOnly = true)
    public List<TravelRequestDto> findAll() {
        return travelRequestRepository.findAll()
                .stream()
                .map(travelRequestMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TravelRequestDto> search(TravelRequestFilter filter, Pageable pageable) {
        return travelRequestSpecificationDao.search(filter, pageable)
                .map(travelRequestMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TravelRequestDto> findByDepartmentCodeJpql(String departmentCode, Pageable pageable) {
        return travelRequestJpqlDao.findByDepartmentCode(departmentCode, pageable)
                .map(travelRequestMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TravelRequestDto> findByStatusAndDestinationCriteria(TravelRequestStatus status,
                                                                   String destination, Pageable pageable) {
        return travelRequestCriteriaDao.findByStatusAndDestination(status, destination, pageable)
                .map(travelRequestMapper::toDto);
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
        TravelRequestStatus oldStatus = request.getStatus();
        if (oldStatus == TravelRequestStatus.APPROVED || oldStatus == TravelRequestStatus.REJECTED) {
            throw new IllegalArgumentException("Travel request in status " + oldStatus + " cannot be changed");
        }

        travelRequestMapper.updateEntity(dto, request);
        setReferences(request, dto.employeeId(), dto.departmentId());

        TravelRequest savedRequest = travelRequestRepository.save(request);
        if (oldStatus != savedRequest.getStatus()) {
            RequestHistory history = new RequestHistory();
            history.setTravelRequest(savedRequest);
            history.setOldStatus(oldStatus);
            history.setNewStatus(savedRequest.getStatus());
            requestHistoryRepository.save(history);
        }

        return travelRequestMapper.toDto(savedRequest);
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
