package com.demo.senla.dto.transport;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransportDto(
        Long id,
        @NotNull TransportType type,
        @NotNull @Size(min = 1, max = 255) String name,
        @Size(min = 1, max = 255) String model,
        @Size(min = 1, max = 255) String airline,
        @Min(1) @Max(100) Integer carriageCount
) {
}
