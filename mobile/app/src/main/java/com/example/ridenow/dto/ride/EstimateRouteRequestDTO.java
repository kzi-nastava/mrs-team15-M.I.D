package com.example.ridenow.dto.ride;

import com.example.ridenow.dto.model.LocationDTO;

import java.io.Serializable;
import java.util.List;

public class EstimateRouteRequestDTO implements Serializable {
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private List<LocationDTO> stops;

    // include address fields to satisfy backend validation
    private String startAddress;
    private String endAddress;
    private java.util.List<String> stopAddresses;
    private java.util.List<Double> stopLatitudes;
    private java.util.List<Double> stopLongitudes;

    public EstimateRouteRequestDTO() {}

    public double getStartLatitude() { return startLatitude; }
    public void setStartLatitude(double startLatitude) { this.startLatitude = startLatitude; }

    public double getStartLongitude() { return startLongitude; }
    public void setStartLongitude(double startLongitude) { this.startLongitude = startLongitude; }

    public double getEndLatitude() { return endLatitude; }
    public void setEndLatitude(double endLatitude) { this.endLatitude = endLatitude; }

    public double getEndLongitude() { return endLongitude; }
    public void setEndLongitude(double endLongitude) { this.endLongitude = endLongitude; }

    public List<LocationDTO> getStops() { return stops; }
    public void setStops(List<LocationDTO> stops) { this.stops = stops; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getEndAddress() { return endAddress; }
    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }

    public java.util.List<String> getStopAddresses() { return stopAddresses; }
    public void setStopAddresses(java.util.List<String> stopAddresses) { this.stopAddresses = stopAddresses; }

    public java.util.List<Double> getStopLatitudes() { return stopLatitudes; }
    public void setStopLatitudes(java.util.List<Double> stopLatitudes) { this.stopLatitudes = stopLatitudes; }

    public java.util.List<Double> getStopLongitudes() { return stopLongitudes; }
    public void setStopLongitudes(java.util.List<Double> stopLongitudes) { this.stopLongitudes = stopLongitudes; }

 }
