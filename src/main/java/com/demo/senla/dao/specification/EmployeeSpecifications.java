package com.demo.senla.dao.specification;

import com.demo.senla.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class EmployeeSpecifications {
    private EmployeeSpecifications() { }

    public static Specification<Employee> fullNameContains(String name) {
        return (root, query, builder) -> name == null || name.isBlank() ? null
                : builder.like(builder.lower(root.get("fullName")), "%" + name.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<Employee> positionContains(String position) {
        return (root, query, builder) -> position == null || position.isBlank() ? null
                : builder.like(builder.lower(root.get("position")), "%" + position.toLowerCase(Locale.ROOT) + "%");
    }
}
