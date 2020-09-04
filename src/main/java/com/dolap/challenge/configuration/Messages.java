package com.dolap.challenge.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * This component is a helper component to read localized messages
 */
@Component
public class Messages {

    @Autowired
    private MessageSourceAccessor accessor;

    /**
     * Reads the localized messages
     *
     * @param code is the key to read from the property file
     * @return the localized message
     */
    public String get(String code) {
        return accessor.getMessage(code);
    }

    /**
     * Reads the localized messages with given locale
     *
     * @param code is the key to read from the property file
     * @param locale is the locale we want to read the message from
     * @return the localized message
     */
    public String get(String code, Locale locale) {
        return accessor.getMessage(code, locale);
    }
}
