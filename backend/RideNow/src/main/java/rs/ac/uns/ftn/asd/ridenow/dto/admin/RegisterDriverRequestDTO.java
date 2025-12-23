package rs.ac.uns.ftn.asd.ridenow.dto.admin;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class RegisterDriverRequestDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String address;

    private String profileImage;

    @NotBlank
    private String licensePlate;

    @NotBlank
    private String vehicleModel;

    @NotNull
    private VehicleType vehicleType;

    @Min(1)
    private int numberOfSeats;

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

    public VehicleType getVehicleType() { return vehicleType; }

    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public int getNumberOfSeats() { return numberOfSeats; }

    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public boolean isBabyFriendly() { return babyFriendly; }

    public void setBabyFriendly(boolean babyFriendly) { this.babyFriendly = babyFriendly; }

    public boolean isPetFriendly() { return petFriendly; }

    public void setPetFriendly(boolean petFriendly) { this.petFriendly = petFriendly; }

}
