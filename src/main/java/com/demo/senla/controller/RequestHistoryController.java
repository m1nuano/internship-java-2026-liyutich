package com.demo.senla.controller;

import com.demo.senla.dto.RequestHistoryDto;
import com.demo.senla.service.RequestHistoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

@RestController
@RequestMapping("/api/request-history")
@RequiredArgsConstructor
public class RequestHistoryController {

    private final RequestHistoryService requestHistoryService;

    @GetMapping
    public Page<RequestHistoryDto> findAll(
            @RequestParam(required = false) @Min(1) Long travelRequestId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Sort sort) {
        PageRequest pageable = PageRequest.of(page, size, sort.isSorted() ? sort : Sort.by("id"));
        return requestHistoryService.findAll(travelRequestId, pageable);
    }

    @GetMapping("/{id}")
    public RequestHistoryDto findById(@PathVariable @Min(1) Long id) {
        return requestHistoryService.findById(id);
    }

    @PostMapping
    public ResponseEntity<RequestHistoryDto> create(@Valid @RequestBody RequestHistoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestHistoryService.create(dto));
    }

    @PutMapping("/{id}")
    public RequestHistoryDto update(@PathVariable @Min(1) Long id, @Valid @RequestBody RequestHistoryDto dto) {
        return requestHistoryService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        requestHistoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
