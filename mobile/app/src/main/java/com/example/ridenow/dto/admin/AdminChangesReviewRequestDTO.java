package com.example.ridenow.dto.admin;

public class AdminChangesReviewRequestDTO {
    private boolean approved;
    private String message;

    public AdminChangesReviewRequestDTO() {}

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
