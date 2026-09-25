package az.abb.loan.common.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;

import az.abb.loan.common.exception.handler.support.ErrorMessageResolver;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

class ErrorMessageResolverTest {

    @Test
    void serviceMessagesWinAndLibraryFillsTheGaps() {
        StaticMessageSource service = new StaticMessageSource();
        service.setUseCodeAsDefaultMessage(true); // must not hide the library fallback
        service.addMessage("error.internal.title", Locale.ENGLISH, "Oops");
        ErrorMessageResolver resolver = new ErrorMessageResolver(service);

        assertThat(resolver.resolve("error.internal.title", null, Locale.ENGLISH)).isEqualTo("Oops");
        assertThat(resolver.resolve("error.internal.message", null, Locale.ENGLISH))
                .isEqualTo("Unexpected internal error");
        assertThat(resolver.resolve("error.method.argument.type.message", new Object[] {"id"}, Locale.forLanguageTag("az")))
                .isEqualTo("id parametri üçün yanlış dəyər verilib");
        assertThat(resolver.resolve("unknown.key", null, Locale.ENGLISH)).isEqualTo("unknown.key");
    }

    @Test
    void unknownLocaleFallsBackToEnglishNotSystemLocale() {
        ErrorMessageResolver resolver = new ErrorMessageResolver(new StaticMessageSource());
        assertThat(resolver.resolve("error.internal.title", null, Locale.JAPANESE)).isEqualTo("Internal Error");
    }
}
