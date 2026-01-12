package az.abb.loan.exception.test.enums;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import az.abb.loan.exception.test.exception.BadRequestException;
import org.springframework.http.HttpStatus;

public enum BadRequestExceptionCode implements ErrorCode {

    BAD_REQUEST_EXCEPTION_CODE("4400", "error.user.title", "error.user.message");

    private final String code;
    private final String titleKey;
    private final String titleMessage;

    BadRequestExceptionCode(String code, String titleKey, String titleMessage) {
        this.code = code;
        this.titleKey = titleKey;
        this.titleMessage = titleMessage;
    }

    @Override
    public String code() {
        return this.code;
    }

    @Override
    public String titleKey() {
        return this.titleKey;
    }

    @Override
    public String messageKey() {
        return this.titleMessage;
    }

    @Override
    public BaseException exception(HttpStatus status, Object... args) {
        return new BadRequestException(this, status, null, args);
    }

    @Override
    public BaseException exceptionWithMessage(HttpStatus status, String message) {
        return new BadRequestException(this, status, message, null);
    }
}
