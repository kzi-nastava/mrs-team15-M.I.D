package com.example.ridenow.dto.user;

import com.google.gson.annotations.SerializedName;

public class UserItemDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String Name;

    @SerializedName("surname")
    private String Surname;

    @SerializedName("email")
    private String Email;

    @SerializedName("role")
    private String Role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSurname() {
        return Surname;
    }

    public void setSurname(String surname) {
        Surname = surname;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }
}
