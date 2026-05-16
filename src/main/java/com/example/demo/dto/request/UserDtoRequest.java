package com.example.demo.dto.request;

public class UserDtoRequest {
    private String username;
    private String email;
    private String password;
    public UserDtoRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    public UserDtoRequest() {
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
