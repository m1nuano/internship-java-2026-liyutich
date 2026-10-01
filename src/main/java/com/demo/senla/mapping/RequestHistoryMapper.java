package com.demo.senla.mapping;

import com.demo.senla.dto.RequestHistoryDto;
import com.demo.senla.entity.RequestHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RequestHistoryMapper {

    @Mapping(source = "travelRequest.id", target = "travelRequestId")
    RequestHistoryDto toDto(RequestHistory history);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "travelRequest", ignore = true)
    RequestHistory toEntity(RequestHistoryDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "travelRequest", ignore = true)
    void updateEntity(RequestHistoryDto dto, @MappingTarget RequestHistory history);
}
