package com.server.server.auth;

public record AuthRequest(String username, String password) {

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }
}
