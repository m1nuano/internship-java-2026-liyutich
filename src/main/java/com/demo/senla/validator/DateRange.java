package com.demo.senla.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface DateRange {
    String message() default "Start date must be on or before end date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
