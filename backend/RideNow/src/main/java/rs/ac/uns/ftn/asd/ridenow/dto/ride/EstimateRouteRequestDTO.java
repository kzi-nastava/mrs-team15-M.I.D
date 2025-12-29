package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
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

}
