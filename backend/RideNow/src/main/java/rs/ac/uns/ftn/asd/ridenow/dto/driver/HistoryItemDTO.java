package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import ch.qos.logback.core.model.INamedModel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter @Setter
public class HistoryItemDTO {
    @NotNull
    private Long routeId;
    @NotNull @NotEmpty
    private String startLocation;
    @NotNull @NotEmpty
    private String endLocation;
    @NotNull @NotEmpty
    private List<String> stopLocations;
    @NotNull @NotEmpty
    private List<String> passengers;
    @NotNull
    private Date rideDate;
    @NotNull
    private Integer durationMinutes;
    @NotNull
    private Double cost;
    @NotNull
    private Boolean cancelled;
    private String cancelledBy;
    @NotNull
    private Boolean panic;
    private String panicBy;
    @NotNull
    private Double rating;
    @NotNull @NotEmpty
    private List<String> inconsistencies;

}
