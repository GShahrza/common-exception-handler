package az.abb.loan.common.exception.handler.exception;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final String detail;
    private final Object[] args;

    protected BaseException(ErrorCode errorCode, HttpStatus status, String detail, Object... args) {
        super(errorCode.messageKey());
        this.errorCode = errorCode;
        this.status = status;
        this.detail = detail;
        this.args = args != null ? args : new Object[0];
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }
    public String getDetail() { return detail; }
    public Object[] getArgs() { return args; }
}
