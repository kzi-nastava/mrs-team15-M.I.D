package rs.ac.uns.ftn.asd.ridenow.dto.auth;

public class RegisterDriverRequestDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profileImage;

    private String licensePlate;
    private String vehicleModel;
    private String vehicleType;
    private int numberOfSeats ;
    private boolean babyFriendly;
    private boolean petFriendly;

    public RegisterDriverRequestDTO() {
        super();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

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

}
