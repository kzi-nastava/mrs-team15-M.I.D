package com.example.ridenow.dto.auth;

public class ActivateAccountRequestDTO {
    private String email;

    public ActivateAccountRequestDTO() {}

    public ActivateAccountRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
