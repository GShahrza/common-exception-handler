package az.abb.loan.common.exception.handler.app;

import az.abb.loan.common.exception.handler.config.ErrorProperties;
import az.abb.loan.common.exception.handler.handler.AbstractGlobalExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    public GlobalExceptionHandler(MessageSource messageSource, ErrorProperties errorProperties) {
        super(messageSource, errorProperties);
    }
}
