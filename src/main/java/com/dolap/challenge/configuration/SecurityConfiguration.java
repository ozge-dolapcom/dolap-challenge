package com.dolap.challenge.configuration;

import com.dolap.challenge.entity.User;
import com.dolap.challenge.filter.JwtFilter;
import com.dolap.challenge.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * Retrieves the user details to authenticate the users with given credentials
     */
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Filter that runs once before each request to validate the token and the roles
     */
    private JwtFilter jwtFilter;

    /**
     * Bean that is used to encyrpt / decrypt the password
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    public SecurityConfiguration(CustomUserDetailsService customUserDetailsService, JwtFilter jwtFilter){
        this.jwtFilter = jwtFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Configures the AuthenticationManager to run with the BcrpytPasswordEncoder and
     * Customer UserDetailsService
     *
     * @param auth AuthenticationManagerBuilder to build the auth manager
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
    }

    /**
     * AuthenticationManagerBean that runs the authentication
     *
     * @return AuthenticationManager that is configured with the configure method above
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Configures the HttpSecurity of the API
     * Whitelists and Blacklists the endpoints depending on the role and the endpoint itself
     * /auth/** are permitted all - and are public
     * /payments are permitted all - and are public
     * GET /products and /products/** are public
     * GET /categories and /categories/** are public
     * POST, PUT and DELETE to /products and /categories are not permitted unless you're authroized as ADMIN
     *
     * @param http Spring Security extension to configure
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/payments").permitAll()
                .antMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                .antMatchers(HttpMethod.GET, "/categories", "/categories/**").permitAll()
                .antMatchers("/products", "/products/**").hasAuthority(User.ROLE_ADMIN)
                .antMatchers("/categories", "/categories/**").hasAuthority(User.ROLE_ADMIN)
                .anyRequest().authenticated()
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
