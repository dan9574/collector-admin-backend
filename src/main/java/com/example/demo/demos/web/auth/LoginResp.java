package com.example.demo.demos.web.auth;

public class LoginResp {
    private String token;
    private String username;
    public LoginResp(String token, String username) {
        this.token = token; this.username = username;
    }
    public String getToken() { return token; }
    public String getUsername() { return username; }
}
