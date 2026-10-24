package com.demo.senla.entity.transport.tableperclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "table_per_class_cars")
@Getter
@Setter
@NoArgsConstructor
public class CarTablePerClass extends TransportTablePerClass {

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false, length = 255)
    private String model;
}
