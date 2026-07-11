package com.ansh.smart_commerce.dto;

public class LoginResponse {

    private String token;
    private Long id;
    private String name;
    private String email;

    public LoginResponse(String token, Long id, String name, String email) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}