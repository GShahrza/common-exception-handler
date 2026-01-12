package az.abb.loan.exception.test.exception;

import static az.abb.loan.exception.test.enums.BadRequestExceptionCode.BAD_REQUEST_EXCEPTION_CODE;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {

    public NotFoundException(ErrorCode errorCode, Object[] args) {
        super(errorCode, HttpStatus.BAD_REQUEST, null, args);
    }

    public NotFoundException(ErrorCode errorCode, String detail, Object[] args) {
        super(errorCode, HttpStatus.BAD_REQUEST, detail, args);
    }

    public NotFoundException(ErrorCode errorCode, HttpStatus status, String detail, Object[] args) {
        super(errorCode, status, detail, args);
    }
}
