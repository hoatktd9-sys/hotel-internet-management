package com.example.demo.dto.request;

public class UserDtoRequest {
    private String username;
    private String email;
    public UserDtoRequest(String username, String email) {
        this.username = username;
        this.email = email;
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
