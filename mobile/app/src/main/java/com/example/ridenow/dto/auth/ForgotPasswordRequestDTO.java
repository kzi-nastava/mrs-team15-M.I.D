package com.example.ridenow.dto.auth;

public class ForgotPasswordRequestDTO {
    private String email;

    public ForgotPasswordRequestDTO() {}

    public ForgotPasswordRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
