package com.demo.senla.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {

    private static final String START_DATE_FIELD = "startDate";
    private static final String END_DATE_FIELD = "endDate";

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Field startField = value.getClass().getDeclaredField(START_DATE_FIELD);
            Field endField = value.getClass().getDeclaredField(END_DATE_FIELD);
            startField.setAccessible(true);
            endField.setAccessible(true);

            LocalDate startDate = (LocalDate) startField.get(value);
            LocalDate endDate = (LocalDate) endField.get(value);
            return startDate == null || endDate == null || !startDate.isAfter(endDate);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new ValidationException("Cannot read " + START_DATE_FIELD + " and " + END_DATE_FIELD + " fields", exception);
        }
    }
}
