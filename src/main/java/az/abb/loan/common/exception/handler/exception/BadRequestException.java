package az.abb.loan.common.exception.handler.exception;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException(ErrorCode errorCode, Object[] args) {
        super(errorCode, HttpStatus.BAD_REQUEST, null, args);
    }

    public BadRequestException(ErrorCode errorCode, HttpStatus status, Object[] args) {
        super(errorCode, status, null, args);
    }

    public BadRequestException(ErrorCode errorCode, HttpStatus status, String detail, Object[] args) {
        super(errorCode, status, detail, args);
    }

}
