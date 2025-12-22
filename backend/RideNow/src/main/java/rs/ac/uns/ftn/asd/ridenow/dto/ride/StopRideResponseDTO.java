package rs.ac.uns.ftn.asd.ridenow.dto.ride;

public class StopRideResponseDTO {
    private double price;
    private String endLocation;

    public StopRideResponseDTO() {
        super();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }
}
