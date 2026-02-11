package com.example.ridenow.dto.ride;

import java.io.Serializable;
import java.util.List;

public class FavoriteRouteResponseDTO implements Serializable {
    private Long routeId;
    private String startAddress;
    private String endAddress;
    private List<String> stopAddresses;

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getEndAddress() { return endAddress; }
    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }

    public List<String> getStopAddresses() { return stopAddresses; }
    public void setStopAddresses(List<String> stopAddresses) { this.stopAddresses = stopAddresses; }
}
