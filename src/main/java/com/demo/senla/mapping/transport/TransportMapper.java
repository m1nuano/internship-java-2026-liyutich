package com.demo.senla.mapping.transport;

import com.demo.senla.dto.transport.TransportDto;

public interface TransportMapper<T> {

    T toCar(TransportDto dto);

    T toPlane(TransportDto dto);

    T toTrain(TransportDto dto);

    default T toEntity(TransportDto dto) {
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
