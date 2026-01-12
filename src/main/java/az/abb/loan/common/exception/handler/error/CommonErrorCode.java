package az.abb.loan.common.exception.handler.error;

import az.abb.loan.common.exception.handler.exception.BadRequestException;
import az.abb.loan.common.exception.handler.exception.BaseException;
import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {

    CLIENT_ERROR("4000", "Client Error", "Error occurred when call client"),
    VALIDATION_ERROR(
            "1000", "error.validation.title", "error.validation.message"
    ),
    JSON_PARSE_ERROR(
            "1001", "error.json.parse.title", "error.json.parse.message"
    ),
    CONSTRAINT_VIOLATION(
            "1002", "error.constraint.violation.title", "error.constraint.violation.message"
    ),
    METHOD_ARGUMENT_TYPE_MISMATCH(
            "1003", "error.method.argument.type.title", "error.method.argument.type.message"
    ),
    DATA_INTEGRITY_VIOLATION(
            "2001", "error.data.integrity.title", "error.data.integrity.message"
    ),
    OPTIMISTIC_LOCK(
            "2002", "error.optimistic.lock.title", "error.optimistic.lock.message"
    ),
    PESSIMISTIC_LOCK(
            "2003", "error.pessimistic.lock.title", "error.pessimistic.lock.message"
    ),
    RESOURCE_NOT_FOUND(
            "3001", "error.resource.not.found.title", "error.resource.not.found.message"
    ),
    METHOD_NOT_ALLOWED(
            "3002", "error.method.not.allowed.title", "error.method.not.allowed.message"
    ),
    ACCESS_DENIED(
            "4003", "error.access.denied.title", "error.access.denied.message"
    ),
    UNAUTHORIZED(
            "4001", "error.unauthorized.title", "error.unauthorized.message"
    ),
    INTERNAL_ERROR(
            "9999", "error.internal.title", "error.internal.message"
    );

    private final String code;
    private final String titleKey;
    private final String messageKey;

    CommonErrorCode(String code, String titleKey, String messageKey) {
        this.code = code;
        this.titleKey = titleKey;
        this.messageKey = messageKey;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String titleKey() {
        return titleKey;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    @Override
    public BaseException exception(HttpStatus status, Object... args) {
        return new BadRequestException(this, status, args);
    }

    @Override
    public BaseException exceptionWithMessage(HttpStatus status, String message) {
        return new BadRequestException(this, status, null);
    }
}
