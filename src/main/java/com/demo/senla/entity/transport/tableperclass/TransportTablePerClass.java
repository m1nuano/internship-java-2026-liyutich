package com.demo.senla.entity.transport.tableperclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SequenceGenerator(name = "tablePerClassTransportSequence", sequenceName = "table_per_class_transport_seq", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
public abstract class TransportTablePerClass {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tablePerClassTransportSequence")
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false, length = 255)
    private String name;
}
