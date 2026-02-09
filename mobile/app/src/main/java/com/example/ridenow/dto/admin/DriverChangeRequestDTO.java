package com.example.ridenow.dto.admin;

public class DriverChangeRequestDTO {
    private long requestId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profileImage;
    private String licensePlate;
    private String vehicleModel;
    private String vehicleType;
    private int numberOfSeats;
    private boolean babyFriendly;
    private boolean petFriendly;
    private String status;
    private String submitDate;
    private Long driverId;
    private long id;

    public DriverChangeRequestDTO() {}

    private String message;
    private String adminResponseDate;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAdminResponseDate() { return adminResponseDate; }
    public void setAdminResponseDate(String adminResponseDate) { this.adminResponseDate = adminResponseDate; }

    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public boolean isBabyFriendly() { return babyFriendly; }
    public void setBabyFriendly(boolean babyFriendly) { this.babyFriendly = babyFriendly; }

    public boolean isPetFriendly() { return petFriendly; }
    public void setPetFriendly(boolean petFriendly) { this.petFriendly = petFriendly; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmitDate() { return submitDate; }
    public void setSubmitDate(String submitDate) { this.submitDate = submitDate; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
}
