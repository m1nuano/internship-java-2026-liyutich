package com.demo.senla.dao.specification;

import com.demo.senla.entity.Employee;
import com.demo.senla.entity.Employee_;
import com.demo.senla.entity.Department_;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public final class EmployeeSpecifications {
    private EmployeeSpecifications() { }

    public static Specification<Employee> fullNameContains(String name) {
        return (root, query, builder) -> name == null || name.isBlank() ? null
                : builder.like(builder.lower(root.get(Employee_.fullName)), "%" + name.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<Employee> positionContains(String position) {
        return (root, query, builder) -> position == null || position.isBlank() ? null
                : builder.like(builder.lower(root.get(Employee_.position)), "%" + position.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<Employee> departmentIdEquals(Long departmentId) {
        return (root, query, builder) -> departmentId == null ? null
                : builder.equal(root.get(Employee_.department).get(Department_.id), departmentId);
    }

    public static Specification<Employee> hireDateFrom(LocalDate date) {
        return (root, query, builder) -> date == null ? null
                : builder.greaterThanOrEqualTo(root.get(Employee_.hireDate), date);
    }

    public static Specification<Employee> hireDateTo(LocalDate date) {
        return (root, query, builder) -> date == null ? null
                : builder.lessThanOrEqualTo(root.get(Employee_.hireDate), date);
    }
}
