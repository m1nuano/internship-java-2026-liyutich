package com.demo.senla.service;

import com.demo.senla.dto.DepartmentDto;
import com.demo.senla.entity.Department;
import com.demo.senla.mapping.DepartmentMapper;
import com.demo.senla.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional(readOnly = true)
    public List<DepartmentDto> findAll() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentDto findById(Long id) {
        return departmentMapper.toDto(findEntity(id));
    }

    public DepartmentDto create(DepartmentDto dto) {
        Department department = departmentMapper.toEntity(dto);

        return departmentMapper.toDto(departmentRepository.save(department));
    }

    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department department = findEntity(id);
        departmentMapper.updateEntity(dto, department);

        return departmentMapper.toDto(departmentRepository.save(department));
    }

    public void delete(Long id) {
        Department department = findEntity(id);
        departmentRepository.delete(department);
    }

    private Department findEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + id));
    }
}
