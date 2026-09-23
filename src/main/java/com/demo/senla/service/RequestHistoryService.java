package com.demo.senla.service;

import com.demo.senla.dto.RequestHistoryDto;
import com.demo.senla.entity.RequestHistory;
import com.demo.senla.entity.TravelRequest;
import com.demo.senla.repository.RequestHistoryRepository;
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
public class RequestHistoryService {

    private final RequestHistoryRepository requestHistoryRepository;
    private final TravelRequestRepository travelRequestRepository;

    @Transactional(readOnly = true)
    public List<RequestHistoryDto> findAll() {
        List<RequestHistory> historyList = requestHistoryRepository.findAll();
        List<RequestHistoryDto> result = new ArrayList<>();

        for (RequestHistory history : historyList) {
            result.add(toDto(history));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public RequestHistoryDto findById(Long id) {
        return toDto(findEntity(id));
    }

    public RequestHistoryDto create(RequestHistoryDto dto) {
        RequestHistory history = new RequestHistory();
        fillHistory(history, dto);

        RequestHistory savedHistory = requestHistoryRepository.save(history);
        return toDto(savedHistory);
    }

    public RequestHistoryDto update(Long id, RequestHistoryDto dto) {
        RequestHistory history = findEntity(id);
        fillHistory(history, dto);

        RequestHistory savedHistory = requestHistoryRepository.save(history);
        return toDto(savedHistory);
    }

    public void delete(Long id) {
        RequestHistory history = findEntity(id);
        requestHistoryRepository.delete(history);
    }

    private RequestHistory findEntity(Long id) {
        return requestHistoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request history not found: " + id));
    }

    private void fillHistory(RequestHistory history, RequestHistoryDto dto) {
        history.setOldStatus(dto.getOldStatus());
        history.setNewStatus(dto.getNewStatus());
        history.setChangedAt(dto.getChangedAt());
        history.setComment(dto.getComment());

        if (dto.getTravelRequestId() == null) {
            history.setTravelRequest(null);
        } else {
            TravelRequest request = travelRequestRepository.findById(dto.getTravelRequestId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Travel request not found: " + dto.getTravelRequestId()));
            history.setTravelRequest(request);
        }
    }

    private RequestHistoryDto toDto(RequestHistory history) {
        Long travelRequestId = history.getTravelRequest() == null
                ? null
                : history.getTravelRequest().getId();
        return new RequestHistoryDto(
                history.getId(),
                travelRequestId,
                history.getOldStatus(),
                history.getNewStatus(),
                history.getChangedAt(),
                history.getComment()
        );
    }
}
