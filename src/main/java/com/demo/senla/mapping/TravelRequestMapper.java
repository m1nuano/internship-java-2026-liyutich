package com.demo.senla.mapping;

import com.demo.senla.dto.TravelRequestDto;
import com.demo.senla.entity.TravelRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TravelRequestMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "department.id", target = "departmentId")
    TravelRequestDto toDto(TravelRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TravelRequest toEntity(TravelRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TravelRequestDto dto, @MappingTarget TravelRequest request);
}
