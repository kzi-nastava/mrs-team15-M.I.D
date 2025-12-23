package rs.ac.uns.ftn.asd.ridenow.dto.admin;

public class RideDetailsDTO {
    private Long rideId;
    private String route;
    private String driver;
    private String passenger;
    private double price;
    private boolean panicTriggered;
    private String inconsistencies;
    private double rating;

    public RideDetailsDTO() {
        super();
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPassenger() {
        return passenger;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isPanicTriggered() {
        return panicTriggered;
    }

    public void setPanicTriggered(boolean panicTriggered) {
        this.panicTriggered = panicTriggered;
    }

    public String getInconsistencies() {
        return inconsistencies;
    }

    public void setInconsistencies(String inconsistencies) {
        this.inconsistencies = inconsistencies;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
