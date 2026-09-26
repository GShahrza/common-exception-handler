package io.github.gshahrza.exceptionhandler.config;

import io.github.gshahrza.exceptionhandler.decoder.CommonFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@EnableConfigurationProperties(ErrorProperties.class)
public class ExceptionAutoConfiguration {

    /** Registers the decoder for all Feign clients unless the service defines its own ErrorDecoder. */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ErrorDecoder.class, ObjectMapper.class})
    @ConditionalOnProperty(prefix = "common.error.feign", name = "enabled", matchIfMissing = true)
    static class FeignErrorDecoderConfiguration {

        @Bean
        @ConditionalOnMissingBean(ErrorDecoder.class)
        CommonFeignErrorDecoder commonFeignErrorDecoder(ObjectProvider<ObjectMapper> objectMapper) {
            return new CommonFeignErrorDecoder(objectMapper.getIfAvailable(
                    () -> tools.jackson.databind.json.JsonMapper.builder().build()));
        }
    }
}
