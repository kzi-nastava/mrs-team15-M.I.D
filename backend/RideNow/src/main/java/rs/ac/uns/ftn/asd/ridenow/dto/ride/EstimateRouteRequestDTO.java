package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class EstimateRouteRequestDTO {

    @NotBlank
    private String startAddress;

    @NotNull
    private Double startLatitude;

    @NotNull
    private Double startLongitude;

    @NotBlank
    private String endAddress;

    @NotNull
    private Double endLatitude;

    @NotNull
    private Double endLongitude;

    private List<@NotBlank String> stopAddresses;

    private List<@NotNull Double> stopLatitudes;

    private List<@NotNull Double> stopLongitudes;

    public EstimateRouteRequestDTO() { super(); }

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

}
