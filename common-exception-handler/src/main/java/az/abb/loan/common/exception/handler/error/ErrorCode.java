package az.abb.loan.common.exception.handler.error;

import az.abb.loan.common.exception.handler.exception.BaseException;
import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String code();

    String titleKey();

    String messageKey();

    /**
     * HTTP status used when the exception is created without an explicit status.
     * Override in your own enums (e.g. NOT_FOUND for "not found" codes).
     */
    default HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    BaseException exception(HttpStatus status, Object... args);

    default BaseException exception(Object... args) {
        return exception(httpStatus(), args);
    }

    BaseException exceptionWithMessage(HttpStatus status, String message);

    default BaseException exceptionWithMessage(String message) {
        return exceptionWithMessage(httpStatus(), message);
    }

}
