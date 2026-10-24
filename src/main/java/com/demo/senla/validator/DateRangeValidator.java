package com.demo.senla.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Field startField = value.getClass().getDeclaredField("startDate");
            Field endField = value.getClass().getDeclaredField("endDate");
            startField.setAccessible(true);
            endField.setAccessible(true);

            LocalDate startDate = (LocalDate) startField.get(value);
            LocalDate endDate = (LocalDate) endField.get(value);
            return startDate == null || endDate == null || !startDate.isAfter(endDate);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new ValidationException("Cannot read startDate and endDate fields", exception);
        }
    }
}
