package com.example.ridenow.dto.driver;

import com.example.ridenow.dto.enums.DriverStatus;

public class DriverStatusResponseDTO {
    DriverStatus status;
    DriverStatus pendingStatus;

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public DriverStatus getPendingStatus() {
        return pendingStatus;
    }

    public void setPendingStatus(DriverStatus pendingStatus) {
        this.pendingStatus = pendingStatus;
    }
}
