package az.abb.loan.exception.test.enums;

import az.abb.loan.common.exception.handler.error.ErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import az.abb.loan.exception.test.exception.FeignClientException;
import org.springframework.http.HttpStatus;

public enum FeignClientErrorCode implements ErrorCode {

    CLIENT_ERROR_CODE("4800", "Client Error", "Exception occurred when call client");

    private final String code;
    private final String titleKey;
    private final String messageKey;

    FeignClientErrorCode(String code, String titleKey, String messageKey) {
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
        return new FeignClientException(this, status, null, args);
    }

    @Override
    public BaseException exception(Object... args) {
        return new FeignClientException(this, args);
    }

    @Override
    public BaseException exceptionWithMessage(HttpStatus status, String message) {
        return new FeignClientException(this, status, message);
    }

    @Override
    public BaseException exceptionWithMessage(String message) {
        return new FeignClientException(this, message);
    }

}
