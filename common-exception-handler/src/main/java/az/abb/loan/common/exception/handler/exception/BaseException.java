package az.abb.loan.common.exception.handler.exception;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

    private static final Object[] NO_ARGS = new Object[0];

    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final String detail;
    private final Object[] args;

    protected BaseException(ErrorCode errorCode, HttpStatus status, String detail, Object... args) {
        super(errorCode.code() + ": " + (detail != null ? detail : errorCode.messageKey()));
        this.errorCode = errorCode;
        this.status = status != null ? status : errorCode.httpStatus();
        this.detail = detail;
        this.args = args != null ? args : NO_ARGS;
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }
    public String getDetail() { return detail; }
    public Object[] getArgs() { return args.clone(); }
}
