package com.demo.senla.service.transport;

import com.demo.senla.dto.transport.TransportDto;
import com.demo.senla.entity.transport.tableperclass.TransportTablePerClass;
import com.demo.senla.mapping.transport.TablePerClassTransportMapper;
import com.demo.senla.repository.transport.TablePerClassTransportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TablePerClassTransportService {

    private final TablePerClassTransportRepository repository;
    private final TablePerClassTransportMapper mapper;

    @Transactional(readOnly = true)
    public Page<TransportDto> findAll(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public TransportDto findById(Long id) {
        return mapper.toDto(findEntity(id));
    }

    public TransportDto create(TransportDto dto) {
        TransportTablePerClass transport = mapper.toEntity(dto);
        return mapper.toDto(repository.save(transport));
    }

    public TransportDto update(Long id, TransportDto dto) {
        TransportTablePerClass existing = findEntity(id);
        TransportTablePerClass transport = mapper.toEntity(dto);
        if (existing.getClass() != transport.getClass()) {
            throw new IllegalArgumentException("Transport type cannot be changed");
        }
        transport.setId(id);
        return mapper.toDto(repository.save(transport));
    }

    public void delete(Long id) {
        TransportTablePerClass transport = findEntity(id);
        repository.delete(transport);
    }

    private TransportTablePerClass findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transport not found: " + id));
    }
}
