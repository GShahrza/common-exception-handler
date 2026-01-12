package az.abb.loan.exception.test.exception;

import static az.abb.loan.exception.test.enums.FeignClientErrorCode.CLIENT_ERROR_CODE;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import org.springframework.http.HttpStatus;

public class FeignClientException extends BaseException {

    public FeignClientException(String detail) {
        super(CLIENT_ERROR_CODE, HttpStatus.BAD_REQUEST, detail, null);
    }

    public FeignClientException(ErrorCode errorCode,Object... args) {
        super(errorCode, HttpStatus.BAD_REQUEST, null, args);
    }

    public FeignClientException(ErrorCode errorCode, String detail) {
        super(errorCode,HttpStatus.BAD_REQUEST, detail, null);
    }

    public FeignClientException(ErrorCode errorCode, HttpStatus status, String detail, Object... args) {
        super(errorCode, status, detail, args);
    }
}
