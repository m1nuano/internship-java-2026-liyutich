package com.demo.senla.mapping.transport;

import com.demo.senla.dto.transport.TransportDto;
import com.demo.senla.entity.transport.singletable.CarSingleTable;
import com.demo.senla.entity.transport.singletable.PlaneSingleTable;
import com.demo.senla.entity.transport.singletable.TrainSingleTable;
import com.demo.senla.entity.transport.singletable.TransportSingleTable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SingleTableTransportMapper {

    @Mapping(target = "type", constant = "CAR")
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "carriageCount", ignore = true)
    TransportDto toCarDto(CarSingleTable car);

    @Mapping(target = "type", constant = "PLANE")
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "carriageCount", ignore = true)
    TransportDto toPlaneDto(PlaneSingleTable plane);

    @Mapping(target = "type", constant = "TRAIN")
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "airline", ignore = true)
    TransportDto toTrainDto(TrainSingleTable train);

    @Mapping(target = "id", ignore = true)
    CarSingleTable toCar(TransportDto dto);

    @Mapping(target = "id", ignore = true)
    PlaneSingleTable toPlane(TransportDto dto);

    @Mapping(target = "id", ignore = true)
    TrainSingleTable toTrain(TransportDto dto);

    default TransportDto toDto(TransportSingleTable transport) {
        if (transport instanceof CarSingleTable car) {
            return toCarDto(car);
        }
        if (transport instanceof PlaneSingleTable plane) {
            return toPlaneDto(plane);
        }
        if (transport instanceof TrainSingleTable train) {
            return toTrainDto(train);
        }
        throw new IllegalArgumentException("Unknown transport type");
    }

    default TransportSingleTable toEntity(TransportDto dto) {
        return switch (dto.type()) {
            case CAR -> {
                if (dto.model() == null) {
                    throw new IllegalArgumentException("model is required for CAR");
                }
                yield toCar(dto);
            }
            case PLANE -> {
                if (dto.airline() == null) {
                    throw new IllegalArgumentException("airline is required for PLANE");
                }
                yield toPlane(dto);
            }
            case TRAIN -> {
                if (dto.carriageCount() == null) {
                    throw new IllegalArgumentException("carriageCount is required for TRAIN");
                }
                yield toTrain(dto);
            }
        };
    }
}

