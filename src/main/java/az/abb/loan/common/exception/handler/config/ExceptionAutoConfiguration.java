package az.abb.loan.common.exception.handler.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ErrorProperties.class)
public class ExceptionAutoConfiguration {
}
