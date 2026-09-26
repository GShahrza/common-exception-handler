package io.github.gshahrza.exceptionhandler.app;

import io.github.gshahrza.exceptionhandler.config.ErrorProperties;
import io.github.gshahrza.exceptionhandler.handler.AbstractGlobalExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    public GlobalExceptionHandler(MessageSource messageSource, ErrorProperties errorProperties) {
        super(messageSource, errorProperties);
    }
}
