package az.abb.loan.exception.test.enums;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import az.abb.loan.exception.test.exception.NotFoundException;
import org.springframework.http.HttpStatus;

public enum NotFoundExceptionCode implements ErrorCode {

    USER_NOT_FOUND("4400", "error.not.found.title", "example with {0}");

    private final String code;
    private final String titleKey;
    private final String messageKey;

    NotFoundExceptionCode(String code, String titleKey, String messageKey) {
        this.code = code;
        this.titleKey = titleKey;
        this.messageKey = messageKey;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String titleKey() {
        return titleKey;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    @Override
    public BaseException exception(HttpStatus status, Object... args) {
        return new NotFoundException(this, status, null, args);
    }

    @Override
    public BaseException exception(Object[] args) {
        return new NotFoundException(this, args);
    }

    @Override
    public BaseException exceptionWithMessage(HttpStatus status, String message) {
        return new NotFoundException(this, message, null);
    }

    @Override
    public BaseException exceptionWithMessage(String message) {
        return new NotFoundException(this, message, null);
    }


}
