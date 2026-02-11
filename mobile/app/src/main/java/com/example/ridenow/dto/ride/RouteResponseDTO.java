package com.example.ridenow.dto.ride;

import java.io.Serializable;
import java.util.List;

public class RouteResponseDTO implements Serializable {
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
    private double priceEstimateStandard;
    private double priceEstimateLuxury;
    private double priceEstimateVan;
    private List<RoutePointDTO> route;

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

    public double getPriceEstimateStandard() { return priceEstimateStandard; }
    public void setPriceEstimateStandard(double priceEstimateStandard) { this.priceEstimateStandard = priceEstimateStandard; }

    public double getPriceEstimateLuxury() { return priceEstimateLuxury; }
    public void setPriceEstimateLuxury(double priceEstimateLuxury) { this.priceEstimateLuxury = priceEstimateLuxury; }

    public double getPriceEstimateVan() { return priceEstimateVan; }
    public void setPriceEstimateVan(double priceEstimateVan) { this.priceEstimateVan = priceEstimateVan; }

    public List<RoutePointDTO> getRoute() { return route; }
    public void setRoute(List<RoutePointDTO> route) { this.route = route; }
}
