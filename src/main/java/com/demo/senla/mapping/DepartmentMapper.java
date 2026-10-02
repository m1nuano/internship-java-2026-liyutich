package com.demo.senla.mapping;

import com.demo.senla.dto.DepartmentDto;
import com.demo.senla.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentDto toDto(Department department);

    @Mapping(target = "id", ignore = true)
    Department toEntity(DepartmentDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(DepartmentDto dto, @MappingTarget Department department);
}
