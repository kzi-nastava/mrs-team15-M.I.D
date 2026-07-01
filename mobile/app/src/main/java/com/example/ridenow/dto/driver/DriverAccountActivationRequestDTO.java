package com.example.ridenow.dto.driver;

public class DriverAccountActivationRequestDTO {
    private String password;
    private String passwordConfirmation;
    private String token;

    public DriverAccountActivationRequestDTO() {}

    public DriverAccountActivationRequestDTO(String password, String passwordConfirmation, String token) {
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}