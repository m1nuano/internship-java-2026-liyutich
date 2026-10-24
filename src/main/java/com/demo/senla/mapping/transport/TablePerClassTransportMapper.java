package com.demo.senla.mapping.transport;

import com.demo.senla.dto.transport.TransportDto;
import com.demo.senla.entity.transport.tableperclass.CarTablePerClass;
import com.demo.senla.entity.transport.tableperclass.PlaneTablePerClass;
import com.demo.senla.entity.transport.tableperclass.TrainTablePerClass;
import com.demo.senla.entity.transport.tableperclass.TransportTablePerClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TablePerClassTransportMapper {

    @Mapping(target = "type", constant = "CAR")
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "carriageCount", ignore = true)
    TransportDto toCarDto(CarTablePerClass car);

    @Mapping(target = "type", constant = "PLANE")
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "carriageCount", ignore = true)
    TransportDto toPlaneDto(PlaneTablePerClass plane);

    @Mapping(target = "type", constant = "TRAIN")
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "airline", ignore = true)
    TransportDto toTrainDto(TrainTablePerClass train);

    @Mapping(target = "id", ignore = true)
    CarTablePerClass toCar(TransportDto dto);

    @Mapping(target = "id", ignore = true)
    PlaneTablePerClass toPlane(TransportDto dto);

    @Mapping(target = "id", ignore = true)
    TrainTablePerClass toTrain(TransportDto dto);

    default TransportDto toDto(TransportTablePerClass transport) {
        if (transport instanceof CarTablePerClass car) {
            return toCarDto(car);
        }
        if (transport instanceof PlaneTablePerClass plane) {
            return toPlaneDto(plane);
        }
        if (transport instanceof TrainTablePerClass train) {
            return toTrainDto(train);
        }
        throw new IllegalArgumentException("Unknown transport type");
    }

    default TransportTablePerClass toEntity(TransportDto dto) {
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
