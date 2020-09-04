package com.dolap.challenge.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

    /**
     * We wishes to be notified of the {@link ApplicationContext} that it runs in.
     * @return the application context we're in
     */
    @Bean
    ApplicationContextProvider applicationContextProvider(){
        return new ApplicationContextProvider();
    }

    /**
     * {@link LocaleResolver} implementation that simply uses the primary locale
     * specified in the "accept-language" header of the HTTP request. Whenever the locale
     * changes in the "accept-language" - we'll be notified of the change and load the messages
     * properly.
     *
     * @return the localeResolver bean
     */
    @Bean
    public LocaleResolver localeResolver(){
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }

    /**
     * Implementation that accesses resource bundles using specified basenames,
     * participating in the Spring {@link ApplicationContext}'s resource loading.
     *
     * @return the message source used to load the resource bundles
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Helper class for easy access to messages from a MessageSource,
     * providing various overloaded getMessage methods.
     *
     * @return an accessor used to retrieve localized messages
     */
    @Bean
    public MessageSourceAccessor messageSourceAccessor(){
        MessageSourceAccessor accessor = new MessageSourceAccessor(messageSource());
        return accessor;
    }

    /**
     * This is the bean validator used and it's configured to use the localized messagesource
     * to raise validation errors with localized messages
     *
     * @return validator factory used to validate beans
     */
    @Bean
    public LocalValidatorFactoryBean getValidator(){
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource());
        return validatorFactoryBean;
    }
}
