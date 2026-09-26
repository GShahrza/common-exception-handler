package io.github.gshahrza.exceptionhandler.config;

import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "common.error")
public class ErrorProperties {

    /**
     * Service prefix added to every error code, e.g. "ORDERS" gives keys like "ORDERS1000".
     */
    private String serviceKey = "";

    /**
     * Locale used when the request has no Accept-Language header.
     */
    private Locale defaultLocale = Locale.ENGLISH;

    private final Feign feign = new Feign();

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey != null ? serviceKey : ""; }

    public Locale getDefaultLocale() { return defaultLocale; }
    public void setDefaultLocale(Locale defaultLocale) { this.defaultLocale = defaultLocale; }

    public Feign getFeign() { return feign; }

    public static class Feign {

        /**
         * Whether to register {@code CommonFeignErrorDecoder} as the default Feign error decoder.
         */
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
