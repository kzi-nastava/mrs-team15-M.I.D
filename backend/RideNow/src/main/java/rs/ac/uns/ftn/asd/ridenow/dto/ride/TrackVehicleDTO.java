package rs.ac.uns.ftn.asd.ridenow.dto.ride;

public class TrackVehicleDTO {
    private double latitude;
    private double longitude;
    private int remainingTimeInMinutes;

    public TrackVehicleDTO() {
        super();
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRemainingTimeInMinutes() {
        return remainingTimeInMinutes;
    }
    public void setRemainingTimeInMinutes(int remainingTimeInMinutes) {
        this.remainingTimeInMinutes = remainingTimeInMinutes;
    }
}
