package com.demo.senla.mapping.transport;

import com.demo.senla.dto.transport.TransportDto;
import com.demo.senla.entity.transport.joined.CarJoined;
import com.demo.senla.entity.transport.joined.PlaneJoined;
import com.demo.senla.entity.transport.joined.TrainJoined;
import com.demo.senla.entity.transport.joined.TransportJoined;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JoinedTransportMapper extends TransportMapper<TransportJoined> {

    @Mapping(target = "type", constant = "CAR")
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "carriageCount", ignore = true)
    TransportDto toCarDto(CarJoined car);

    @Mapping(target = "type", constant = "PLANE")
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "carriageCount", ignore = true)
    TransportDto toPlaneDto(PlaneJoined plane);

    @Mapping(target = "type", constant = "TRAIN")
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "airline", ignore = true)
    TransportDto toTrainDto(TrainJoined train);

    @Mapping(target = "id", ignore = true)
    CarJoined toCar(TransportDto dto);

    @Mapping(target = "id", ignore = true)
    PlaneJoined toPlane(TransportDto dto);

    @Mapping(target = "id", ignore = true)
    TrainJoined toTrain(TransportDto dto);

    default TransportDto toDto(TransportJoined transport) {
        if (transport instanceof CarJoined car) {
            return toCarDto(car);
        }
        if (transport instanceof PlaneJoined plane) {
            return toPlaneDto(plane);
        }
        if (transport instanceof TrainJoined train) {
            return toTrainDto(train);
        }
        throw new IllegalArgumentException("Unknown transport type");
    }
}

