package com.example.ridenow.dto.driver;

import com.example.ridenow.dto.enums.DriverStatus;

public class DriverStatusRequestDTO{
    DriverStatus status;

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }
}
