package az.abb.loan.common.exception.handler.model;

public record FieldErrorResponse(
        String field,
        String message
) {}
