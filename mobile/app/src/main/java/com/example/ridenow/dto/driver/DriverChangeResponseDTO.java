package com.example.ridenow.dto.driver;

public class DriverChangeResponseDTO {
    private Long requestId;
    private String status;
    private String message;

    public DriverChangeResponseDTO() {}

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

