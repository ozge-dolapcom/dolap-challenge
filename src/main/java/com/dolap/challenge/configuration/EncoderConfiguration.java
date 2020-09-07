package com.dolap.challenge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncoderConfiguration {

    /**
     * PasswordEncoder that is used to the encrypt passwords
     * Configured as BCryptPasswordEncoder which adds the dynamic salt itself
     * so no need to worry about that part
     *
     * @return a PasswordEncoder to use in the app
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
