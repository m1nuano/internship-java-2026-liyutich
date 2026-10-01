package com.demo.senla.service;

import com.demo.senla.dto.RequestHistoryDto;
import com.demo.senla.entity.RequestHistory;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.mapping.RequestHistoryMapper;
import com.demo.senla.repository.RequestHistoryRepository;
import com.demo.senla.repository.TravelRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestHistoryService {

    private final RequestHistoryRepository requestHistoryRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final RequestHistoryMapper requestHistoryMapper;

    @Transactional(readOnly = true)
    public List<RequestHistoryDto> findAll() {
        return requestHistoryRepository.findAll()
                .stream()
                .map(requestHistoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequestHistoryDto findById(Long id) {
        return requestHistoryMapper.toDto(findEntity(id));
    }

    public RequestHistoryDto create(RequestHistoryDto dto) {
        RequestHistory history = requestHistoryMapper.toEntity(dto);
        setTravelRequest(history, dto.travelRequestId());

        return requestHistoryMapper.toDto(requestHistoryRepository.save(history));
    }

    public RequestHistoryDto update(Long id, RequestHistoryDto dto) {
        RequestHistory history = findEntity(id);
        requestHistoryMapper.updateEntity(dto, history);
        setTravelRequest(history, dto.travelRequestId());

        return requestHistoryMapper.toDto(requestHistoryRepository.save(history));
    }

    public void delete(Long id) {
        RequestHistory history = findEntity(id);
        requestHistoryRepository.delete(history);
    }

    private RequestHistory findEntity(Long id) {
        return requestHistoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request history not found: " + id));
    }

    private void setTravelRequest(RequestHistory history, Long travelRequestId) {
        if (travelRequestId == null) {
            throw new IllegalArgumentException("Travel request ID is required for request history");
        }

        TravelRequest request = travelRequestRepository.findById(travelRequestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Travel request not found: " + travelRequestId));
        history.setTravelRequest(request);
    }
}
