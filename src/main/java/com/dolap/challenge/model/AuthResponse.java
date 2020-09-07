package com.dolap.challenge.model;

/**
 * Wrapper object that's returned that contains the JWT token
 * When an account is authenticated
 */
public class AuthResponse {
    /**
     * Jwt token that's created for the provided credentials by the AuthRequest
     */
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
