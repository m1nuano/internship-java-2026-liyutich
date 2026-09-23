package com.demo.senla.dto;

import com.demo.senla.entity.TravelRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistoryDto {
    private Long id;
    private Long travelRequestId;
    private TravelRequestStatus oldStatus;
    private TravelRequestStatus newStatus;
    private LocalDateTime changedAt;
    private String comment;
}
