package rs.ac.uns.ftn.asd.ridenow.model;

public class Location {

    private Long id;
    private double latitude;
    private double longitude;
    private String address;

    public Location(Long id, double latitude, double longitude, String address) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }
}
