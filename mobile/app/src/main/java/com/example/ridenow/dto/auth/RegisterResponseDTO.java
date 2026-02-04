package com.example.ridenow.dto.auth;

public class RegisterResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;

    public RegisterResponseDTO() {}

    public RegisterResponseDTO(Long id, String email, String firstName, String lastName, boolean active) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }
}