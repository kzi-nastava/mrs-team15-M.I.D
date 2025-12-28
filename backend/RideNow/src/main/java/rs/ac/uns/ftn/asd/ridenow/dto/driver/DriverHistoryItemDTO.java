package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Route;

import java.util.Date;
import java.util.List;

@Getter @Setter
public class DriverHistoryItemDTO {
    @NotNull
    private Long routeId;
    @NotNull @NotEmpty
    private Route route;
    @NotNull @NotEmpty
    private List<String> passengers;
    @NotNull
    private Date date;
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
