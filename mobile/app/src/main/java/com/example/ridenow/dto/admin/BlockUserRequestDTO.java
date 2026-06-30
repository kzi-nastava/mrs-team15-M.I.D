package com.example.ridenow.dto.admin;

public class BlockUserRequestDTO {

    private String reason;

    public BlockUserRequestDTO() {}

    public BlockUserRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}