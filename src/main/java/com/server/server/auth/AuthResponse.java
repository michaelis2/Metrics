package com.server.server.auth;

public record AuthResponse(String username, String role, String token) {
    public AuthResponse(String username, String role, String token) {
        this.username = username;
        this.role = role;
        this.token = token;
    }

    public String username() {
        return this.username;
    }

    public String role() {
        return this.role;
    }

    public String token() {
        return this.token;
    }
}

