package io.github.gshahrza.exceptionhandler;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gshahrza.exceptionhandler.config.ErrorProperties;
import io.github.gshahrza.exceptionhandler.config.ExceptionAutoConfiguration;
import io.github.gshahrza.exceptionhandler.decoder.CommonFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExceptionAutoConfiguration.class));

    @Test
    void registersPropertiesAndFeignDecoder() {
        runner.withPropertyValues("common.error.service-key=ABC", "common.error.default-locale=az")
                .run(ctx -> {
                    ErrorProperties props = ctx.getBean(ErrorProperties.class);
                    assertThat(props.getServiceKey()).isEqualTo("ABC");
                    assertThat(props.getDefaultLocale()).isEqualTo(Locale.forLanguageTag("az"));
                    assertThat(ctx).hasSingleBean(CommonFeignErrorDecoder.class);
                });
    }

    @Test
    void backsOffWhenServiceDefinesItsOwnDecoderOrDisablesIt() {
        runner.withBean(ErrorDecoder.class, ErrorDecoder.Default::new)
                .run(ctx -> assertThat(ctx).doesNotHaveBean(CommonFeignErrorDecoder.class));
        runner.withPropertyValues("common.error.feign.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(CommonFeignErrorDecoder.class));
    }

    @Test
    void serviceKeyDefaultsToEmptyInsteadOfNull() {
        runner.run(ctx -> assertThat(ctx.getBean(ErrorProperties.class).getServiceKey()).isEmpty());
    }
}
