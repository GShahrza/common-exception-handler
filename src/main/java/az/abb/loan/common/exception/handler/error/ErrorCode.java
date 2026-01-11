package az.abb.loan.common.exception.handler.error;

import az.abb.loan.common.exception.handler.exception.BaseException;
import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String code();

    String titleKey();

    String messageKey();

    BaseException exception(HttpStatus status, Object... args);

    default BaseException exception(Object... args) {
        return exception(HttpStatus.BAD_REQUEST, args);
    }

    BaseException exceptionWithMessage(HttpStatus status, String message);

    default BaseException exceptionWithMessage(String message) {
        return exceptionWithMessage(HttpStatus.BAD_REQUEST, message);
    }

}
