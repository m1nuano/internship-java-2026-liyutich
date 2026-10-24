package com.demo.senla.controller.transport;

import com.demo.senla.dto.transport.TransportDto;
import com.demo.senla.service.transport.SingleTableTransportService;
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
@RequestMapping("/api/transports/single-table")
@RequiredArgsConstructor
public class SingleTableTransportController {

    private final SingleTableTransportService service;

    @GetMapping
    public Page<TransportDto> findAll(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Sort sort) {
        PageRequest pageable = PageRequest.of(page, size, sort.isSorted() ? sort : Sort.by("id"));
        return service.findAll(name, pageable);
    }

    @GetMapping("/{id}")
    public TransportDto findById(@PathVariable @Min(1) Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TransportDto> create(@Valid @RequestBody TransportDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public TransportDto update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody TransportDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

