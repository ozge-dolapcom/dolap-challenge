package com.dolap.challenge.controller;

import com.dolap.challenge.entity.User;
import com.dolap.challenge.model.AuthRequest;
import com.dolap.challenge.model.AuthResponse;
import com.dolap.challenge.service.CustomUserDetailsService;
import com.dolap.challenge.service.UserService;
import com.dolap.challenge.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Used when logging in and signing up
     * Checks whether the provided credentials are matching up to a user or no
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Used to create a token when logging in and signing up
     */
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Creates a token from the given UserDetails object
     */
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    /**
     * Creates a JWT token and returns when valid credentials are provided
     * Expects to receive a username and password
     *
     * @param authRequest a wrapper object that contains username and password
     * @return JWT token when provided credentials are valid
     */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException badCredentialsException) {
            throw new RuntimeException("Invalid username or password");
        }

        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(authRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        return new AuthResponse(token);
    }

    /**
     * Super simple registration endpoint
     * Expects to receive a username and a password
     * No constraints except that the username should be unique 
     * Creates a user and stores in the database and authenticates it and return a JWT token
     * 
     * @param authRequest a wrapper object contains a username and a password
     * @return a JWT token when valid credentials are provided
     */
    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest authRequest) {
        User user = userService.register(authRequest.getUsername(), authRequest.getPassword(), authRequest.getRole());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException badCredentialsException) {
            throw new RuntimeException("Invalid username or password");
        }

        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(authRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        return new AuthResponse(token);
    }

    /**
     * Retrieve user information
     * A nice way to validate your against with
     *
     * @param request is the injected Http request
     * @return UserDetails object that is authenticated against the JWTfilter
     */
    @GetMapping("/me")
    public UserDetails me(HttpServletRequest request) {
        return (UserDetails) request.getAttribute("user");
    }
}
