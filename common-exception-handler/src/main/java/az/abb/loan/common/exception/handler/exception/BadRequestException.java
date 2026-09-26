package az.abb.loan.common.exception.handler.exception;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * General-purpose {@link BaseException}. Despite the historical name, the HTTP status comes from the
 * {@link ErrorCode} (or the explicit {@code status} argument), so it is not limited to 400.
 */
public class BadRequestException extends BaseException {

    public BadRequestException(ErrorCode errorCode, Object... args) {
        super(errorCode, errorCode.httpStatus(), null, args);
    }

    public BadRequestException(ErrorCode errorCode, HttpStatus status, Object... args) {
        super(errorCode, status, null, args);
    }

    public BadRequestException(ErrorCode errorCode, HttpStatus status, String detail, Object... args) {
        super(errorCode, status, detail, args);
    }

}
