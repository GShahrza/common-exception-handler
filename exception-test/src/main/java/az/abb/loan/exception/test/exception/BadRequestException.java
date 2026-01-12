package az.abb.loan.exception.test.exception;

import static az.abb.loan.exception.test.enums.BadRequestExceptionCode.BAD_REQUEST_EXCEPTION_CODE;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException(String detail) {
        super(BAD_REQUEST_EXCEPTION_CODE, HttpStatus.BAD_REQUEST, detail, null);
    }

    public BadRequestException(ErrorCode errorCode, Object[] args) {
        super(errorCode, HttpStatus.BAD_REQUEST, null, args);
    }

    public BadRequestException(ErrorCode errorCode, String detail, Object[] args) {
        super(errorCode, HttpStatus.BAD_REQUEST, detail, args);
    }

    public BadRequestException(ErrorCode errorCode, HttpStatus status, String detail, Object[] args) {
        super(errorCode, status, detail, args);
    }

}
