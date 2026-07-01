package com.example.ridenow.dto.user;

public class BlockedStatusResponseDTO {
    private boolean blocked;
    private String reason;
    private String blockedAt;

    public BlockedStatusResponseDTO() {
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(String blockedAt) {
        this.blockedAt = blockedAt;
    }
}