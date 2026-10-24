package com.demo.senla.entity.transport.singletable;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("TRAIN")
@Getter
@Setter
@NoArgsConstructor
public class TrainSingleTable extends TransportSingleTable {

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "carriage_count")
    private Integer carriageCount;
}

