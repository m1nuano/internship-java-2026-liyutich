package com.demo.senla.entity.transport.singletable;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("PLANE")
@Getter
@Setter
@NoArgsConstructor
public class PlaneSingleTable extends TransportSingleTable {

    @NotNull
    @Size(min = 1, max = 255)
    @Column(length = 255)
    private String airline;
}

