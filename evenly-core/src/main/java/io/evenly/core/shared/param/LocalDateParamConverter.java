package io.evenly.core.shared.param;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ParamConverterProvider for LocalDate to handle date query parameters.
 * Converts ISO date strings (YYYY-MM-DD) to LocalDate.
 */
@Provider
public class LocalDateParamConverter implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == LocalDate.class) {
            @SuppressWarnings("unchecked")
            ParamConverter<T> converter = (ParamConverter<T>) new LocalDateConverter();
            return converter;
        }
        return null;
    }

    private static class LocalDateConverter implements ParamConverter<LocalDate> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public LocalDate fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(value, FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format: " + value + ". Expected format: YYYY-MM-DD", e);
            }
        }

        @Override
        public String toString(LocalDate value) {
            if (value == null) {
                return null;
            }
            return value.format(FORMATTER);
        }
    }
}
