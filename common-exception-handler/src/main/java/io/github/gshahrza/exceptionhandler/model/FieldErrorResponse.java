package io.github.gshahrza.exceptionhandler.model;

public record FieldErrorResponse(
        String field,
        String message
) {}
