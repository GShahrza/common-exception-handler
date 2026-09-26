package io.github.gshahrza.exceptionhandler.support;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Resolves error texts from the service's own {@link MessageSource} first and falls back to the
 * translations bundled with this library ({@code i18n/common-errors*.properties}).
 * Services therefore only need to define keys they want to override or their own error codes.
 */
public class ErrorMessageResolver {

    public static final String LIBRARY_BASENAME = "i18n/common-errors";

    private final MessageSource messageSource;
    private final MessageSource libraryMessages;

    public ErrorMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
        ResourceBundleMessageSource defaults = new ResourceBundleMessageSource();
        defaults.setBasename(LIBRARY_BASENAME);
        defaults.setDefaultEncoding("UTF-8");
        defaults.setFallbackToSystemLocale(false);
        this.libraryMessages = defaults;
    }

    /** Returns the translated message, or the key itself if no translation exists anywhere. */
    public String resolve(String key, Object[] args, Locale locale) {
        String message = find(messageSource, key, args, locale);
        if (message == null) {
            message = find(libraryMessages, key, args, locale);
        }
        return message != null ? message : key;
    }

    public String resolve(MessageSourceResolvable resolvable, Locale locale) {
        try {
            return messageSource.getMessage(resolvable, locale);
        } catch (NoSuchMessageException e) {
            return resolvable.getDefaultMessage();
        }
    }

    private static String find(MessageSource source, String key, Object[] args, Locale locale) {
        String message = source.getMessage(key, args, null, locale);
        // A source configured with useCodeAsDefaultMessage returns the key itself instead of null
        return key.equals(message) ? null : message;
    }
}
