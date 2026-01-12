package az.abb.loan.exception.test.exception.handler;

import az.abb.loan.common.exception.handler.config.ErrorProperties;
import az.abb.loan.common.exception.handler.handler.AbstractGlobalExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@EnableConfigurationProperties(ErrorProperties.class)
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    public GlobalExceptionHandler(MessageSource messageSource, ErrorProperties errorProperties) {
        super(messageSource, errorProperties);
    }

}
