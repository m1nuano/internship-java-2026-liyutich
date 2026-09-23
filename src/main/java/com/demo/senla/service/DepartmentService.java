package com.demo.senla.service;

import com.demo.senla.dto.DepartmentDto;
import com.demo.senla.entity.Department;
import com.demo.senla.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentDto> findAll() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDto> result = new ArrayList<>();

        for (Department department : departments) {
            result.add(toDto(department));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public DepartmentDto findById(Long id) {
        return toDto(findEntity(id));
    }

    public DepartmentDto create(DepartmentDto dto) {
        Department department = new Department();
        department.setName(dto.getName());
        department.setCode(dto.getCode());

        Department savedDepartment = departmentRepository.save(department);
        return toDto(savedDepartment);
    }

    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department department = findEntity(id);
        department.setName(dto.getName());
        department.setCode(dto.getCode());

        Department savedDepartment = departmentRepository.save(department);
        return toDto(savedDepartment);
    }

    public void delete(Long id) {
        Department department = findEntity(id);
        departmentRepository.delete(department);
    }

    private Department findEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + id));
    }

    private DepartmentDto toDto(Department department) {
        return new DepartmentDto(department.getId(), department.getName(), department.getCode());
    }
}
