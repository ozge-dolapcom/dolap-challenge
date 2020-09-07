package com.dolap.challenge.model;

/**
 * Wrapper model to encapsulate login and register information
 */
public class AuthRequest {
    /**
     * Username of the User that's trying to login or register
     */
    private String username;

    /**
     * Password of the User that's trying to login or register
     */
    private String password;

    /**
     * Role of the User that's trying to login or register
     */
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
