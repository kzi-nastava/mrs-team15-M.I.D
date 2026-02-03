package com.example.ridenow.dto.auth;

public class VerifyCodeRequestDTO {
    private String email;
    private String code;

    public VerifyCodeRequestDTO() {}

    public VerifyCodeRequestDTO(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}