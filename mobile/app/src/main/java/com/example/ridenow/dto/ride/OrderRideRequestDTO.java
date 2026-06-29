package com.example.ridenow.dto.ride;

import java.util.List;

public class OrderRideRequestDTO {
    private String startAddress;
    private Double startLatitude;
    private Double startLongitude;
    private String endAddress;
    private Double endLatitude;
    private Double endLongitude;
    private List<String> stopAddresses;
    private List<Double> stopLatitudes;
    private List<Double> stopLongitudes;
    private String vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;
    private List<String> linkedPassengers;
    private String scheduledTime;
    private double distanceKm;
    private int estimatedTimeMinutes;
    private double priceEstimate;
    private List<Double> routeLattitudes;
    private List<Double> routeLongitudes;
    private Long favoriteRouteId;

    public OrderRideRequestDTO() {
    }
    public OrderRideRequestDTO(String startAddress, Double startLatitude, Double startLongitude,
                               String endAddress, Double endLatitude, Double endLongitude,
                               List<String> stopAddresses, List<Double> stopLatitudes, List<Double> stopLongitudes,
                               String vehicleType, boolean babyFriendly, boolean petFriendly,
                               List<String> linkedPassengers, String scheduledTime, double distanceKm,
                               int estimatedTimeMinutes, double priceEstimate,
                               List<Double> routeLattitudes, List<Double> routeLongitudes, Long favoriteRouteId) {
        this.startAddress = startAddress;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endAddress = endAddress;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.stopAddresses = stopAddresses;
        this.stopLatitudes = stopLatitudes;
        this.stopLongitudes = stopLongitudes;
        this.vehicleType = vehicleType;
        this.babyFriendly = babyFriendly;
        this.petFriendly = petFriendly;
        this.linkedPassengers = linkedPassengers;
        this.scheduledTime = scheduledTime;
        this.distanceKm = distanceKm;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.priceEstimate = priceEstimate;
        this.routeLattitudes = routeLattitudes;
        this.routeLongitudes = routeLongitudes;
        this.favoriteRouteId = favoriteRouteId;
    }

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
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public boolean isBabyFriendly() { return babyFriendly; }
    public void setBabyFriendly(boolean babyFriendly) { this.babyFriendly = babyFriendly; }
    public boolean isPetFriendly() { return petFriendly; }
    public void setPetFriendly(boolean petFriendly) { this.petFriendly = petFriendly; }
    public List<String> getLinkedPassengers() { return linkedPassengers; }
    public void setLinkedPassengers(List<String> linkedPassengers) { this.linkedPassengers = linkedPassengers; }
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }
    public double getPriceEstimate() { return priceEstimate; }
    public void setPriceEstimate(double priceEstimate) { this.priceEstimate = priceEstimate; }
    public List<Double> getRouteLattitudes() { return routeLattitudes; }
    public void setRouteLattitudes(List<Double> routeLattitudes) { this.routeLattitudes = routeLattitudes; }
    public List<Double> getRouteLongitudes() { return routeLongitudes; }
    public void setRouteLongitudes(List<Double> routeLongitudes) { this.routeLongitudes = routeLongitudes; }
    public Long getFavoriteRouteId() { return favoriteRouteId; }
    public void setFavoriteRouteId(Long favoriteRouteId) { this.favoriteRouteId = favoriteRouteId; }
}