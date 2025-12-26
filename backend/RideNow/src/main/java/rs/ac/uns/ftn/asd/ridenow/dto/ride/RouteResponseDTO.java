package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class RouteResponseDTO {

    private Long routeId;
    private String startAddress;
    private Double startLatitude;
    private Double startLongitude;
    private String endAddress;
    private Double endLatitude;
    private Double endLongitude;
    private List<String> stopAddresses;
    private List<Double> stopLatitudes;
    private List<Double> stopLongitudes;

    private double distanceKm;
    private int estimatedTimeMinutes;
    private double priceEstimate;

    public RouteResponseDTO() { super(); }

    public Long getRouteId() { return routeId; }

    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getStartAddress() { return startAddress; }

    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public Double getStartLatitude() { return startLatitude; }

    public void setStartLatitude(Double startLatitude) { this.startLatitude = startLatitude; }

    public Double getStartLongitude() { return startLongitude; }

    public void setStartLongitude(Double startLongitude) { this.startLongitude = startLongitude; }

    public String getEndAddress() { return endAddress; }

    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }

    public Double getEndLatitude() { return endLatitude; }

    public void setEndLatitude(Double endLatitude) { this.endLatitude = endLatitude; }

    public Double getEndLongitude() { return endLongitude; }

    public void setEndLongitude(Double endLongitude) { this.endLongitude = endLongitude; }

    public List<String> getStopAddresses() { return stopAddresses; }

    public void setStopAddresses(List<String> stopAddresses) { this.stopAddresses = stopAddresses; }

    public List<Double> getStopLatitudes() { return stopLatitudes; }

    public void setStopLatitudes(List<Double> stopLatitudes) { this.stopLatitudes = stopLatitudes; }

    public List<Double> getStopLongitudes() { return stopLongitudes; }

    public void setStopLongitudes(List<Double> stopLongitudes) { this.stopLongitudes = stopLongitudes; }

    public double getDistanceKm() { return distanceKm; }

    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }

    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    public double getPriceEstimate() { return priceEstimate; }

    public void setPriceEstimate(double priceEstimate) { this.priceEstimate = priceEstimate; }

}
