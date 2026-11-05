package com.demo.senla.controller;

import com.demo.senla.dao.specification.TravelRequestFilter;
import com.demo.senla.dto.TravelRequestDto;
import com.demo.senla.entity.TravelRequestStatus;
import com.demo.senla.service.TravelRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/travel-requests")
@RequiredArgsConstructor
public class TravelRequestController {

    private final TravelRequestService travelRequestService;

    @GetMapping({"", "/specification"})
    public Page<TravelRequestDto> findAll(
            @RequestParam(required = false) TravelRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTo,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @Min(1) Long departmentId,
            @RequestParam(required = false) @Min(1) Long employeeId,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal minCost,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal maxCost,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Sort sort) {
        TravelRequestFilter filter = TravelRequestFilter.builder()
                .status(status)
                .startFrom(startFrom)
                .endTo(endTo)
                .destination(destination)
                .departmentId(departmentId)
                .employeeId(employeeId)
                .minCost(minCost)
                .maxCost(maxCost)
                .build();
        PageRequest pageable = PageRequest.of(page, size, sort.isSorted() ? sort : Sort.by("id"));
        return travelRequestService.search(filter, pageable);
    }

    @GetMapping("/jpql")
    public Page<TravelRequestDto> findByDepartmentCodeJpql(
            @RequestParam @Size(min = 3, max = 10) String departmentCode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return travelRequestService.findByDepartmentCodeJpql(departmentCode, PageRequest.of(page, size));
    }

    @GetMapping("/criteria")
    public Page<TravelRequestDto> findByStatusAndDestinationCriteria(
            @RequestParam(required = false) TravelRequestStatus status,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return travelRequestService.findByStatusAndDestinationCriteria(status, destination, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public TravelRequestDto findById(@PathVariable @Min(1) Long id) {
        return travelRequestService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TravelRequestDto> create(@Valid @RequestBody TravelRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(travelRequestService.create(dto));
    }

    @PutMapping("/{id}")
    public TravelRequestDto update(@PathVariable @Min(1) Long id, @Valid @RequestBody TravelRequestDto dto) {
        return travelRequestService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        travelRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
