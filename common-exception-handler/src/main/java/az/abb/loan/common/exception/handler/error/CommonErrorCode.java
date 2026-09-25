package az.abb.loan.common.exception.handler.error;

import az.abb.loan.common.exception.handler.exception.BadRequestException;
import az.abb.loan.common.exception.handler.exception.BaseException;
import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {

    VALIDATION_ERROR(
            "1000", "error.validation.title", "error.validation.message", HttpStatus.BAD_REQUEST
    ),
    JSON_PARSE_ERROR(
            "1001", "error.json.parse.title", "error.json.parse.message", HttpStatus.BAD_REQUEST
    ),
    CONSTRAINT_VIOLATION(
            "1002", "error.constraint.violation.title", "error.constraint.violation.message", HttpStatus.BAD_REQUEST
    ),
    METHOD_ARGUMENT_TYPE_MISMATCH(
            "1003", "error.method.argument.type.title", "error.method.argument.type.message", HttpStatus.BAD_REQUEST
    ),
    BAD_REQUEST(
            "1004", "error.bad.request.title", "error.bad.request.message", HttpStatus.BAD_REQUEST
    ),
    DATA_INTEGRITY_VIOLATION(
            "2001", "error.data.integrity.title", "error.data.integrity.message", HttpStatus.CONFLICT
    ),
    OPTIMISTIC_LOCK(
            "2002", "error.optimistic.lock.title", "error.optimistic.lock.message", HttpStatus.CONFLICT
    ),
    PESSIMISTIC_LOCK(
            "2003", "error.pessimistic.lock.title", "error.pessimistic.lock.message", HttpStatus.LOCKED
    ),
    RESOURCE_NOT_FOUND(
            "3001", "error.resource.not.found.title", "error.resource.not.found.message", HttpStatus.NOT_FOUND
    ),
    METHOD_NOT_ALLOWED(
            "3002", "error.method.not.allowed.title", "error.method.not.allowed.message", HttpStatus.METHOD_NOT_ALLOWED
    ),
    UNSUPPORTED_MEDIA_TYPE(
            "3003", "error.unsupported.media.type.title", "error.unsupported.media.type.message",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE
    ),
    NOT_ACCEPTABLE(
            "3004", "error.not.acceptable.title", "error.not.acceptable.message", HttpStatus.NOT_ACCEPTABLE
    ),
    CLIENT_ERROR(
            "4000", "error.client.title", "error.client.message", HttpStatus.BAD_GATEWAY
    ),
    UNAUTHORIZED(
            "4001", "error.unauthorized.title", "error.unauthorized.message", HttpStatus.UNAUTHORIZED
    ),
    ACCESS_DENIED(
            "4003", "error.access.denied.title", "error.access.denied.message", HttpStatus.FORBIDDEN
    ),
    INTERNAL_ERROR(
            "9999", "error.internal.title", "error.internal.message", HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String titleKey;
    private final String messageKey;
    private final HttpStatus httpStatus;

    CommonErrorCode(String code, String titleKey, String messageKey, HttpStatus httpStatus) {
        this.code = code;
        this.titleKey = titleKey;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
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
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public BaseException exception(HttpStatus status, Object... args) {
        return new BadRequestException(this, status, args);
    }

    @Override
    public BaseException exceptionWithMessage(HttpStatus status, String message) {
        return new BadRequestException(this, status, message);
    }
}
