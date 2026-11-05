package com.demo.senla.controller;

import com.demo.senla.dto.DepartmentTransferRequest;
import com.demo.senla.dto.EmployeeDto;
import com.demo.senla.dto.EmployeeJdbcCreateRequest;
import com.demo.senla.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping({"", "/specification"})
    public Page<EmployeeDto> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) @Min(1) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireTo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Sort sort) {
        PageRequest pageable = PageRequest.of(page, size, sort.isSorted() ? sort : Sort.by("id"));
        return employeeService.search(name, position, departmentId, hireFrom, hireTo, pageable);
    }

    @GetMapping("/{id}")
    public EmployeeDto findById(@PathVariable @Min(1) Long id) {
        return employeeService.findById(id);
    }

    @GetMapping("/jdbc/{id}")
    public EmployeeDto findByIdJdbc(@PathVariable @Min(1) Long id) {
        return employeeService.findByIdJdbc(id);
    }

    @PutMapping("/jdbc/{id}/department")
    public ResponseEntity<Void> transferToDepartmentJdbc(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody DepartmentTransferRequest request) {
        employeeService.transferToDepartmentJdbc(id, request.departmentId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jdbc-template")
    public Page<EmployeeDto> findByDepartmentJdbcTemplate(
            @RequestParam @Min(1) Long departmentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return employeeService.findByDepartmentJdbcTemplate(departmentId, PageRequest.of(page, size));
    }

    @PostMapping("/jdbc-template")
    public ResponseEntity<Long> createJdbcTemplate(@Valid @RequestBody EmployeeJdbcCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createJdbcTemplate(request));
    }

    @GetMapping("/qbe")
    public Page<EmployeeDto> findByPositionQbe(
            @RequestParam String position,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Sort sort) {
        PageRequest pageable = PageRequest.of(page, size, sort.isSorted() ? sort : Sort.by("id"));
        return employeeService.findByPositionQbe(position, pageable);
    }

    @GetMapping("/spring-data")
    public Page<EmployeeDto> findPageSpringData(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Sort sort) {
        PageRequest pageable = PageRequest.of(page, size, sort.isSorted() ? sort : Sort.by("id"));
        return employeeService.findPageSpringData(pageable);
    }

    @GetMapping("/spring-data/with-department")
    public List<EmployeeDto> findAllWithDepartment() {
        return employeeService.findAllWithDepartment();
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(dto));
    }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable @Min(1) Long id, @Valid @RequestBody EmployeeDto dto) {
        return employeeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
