package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class DriverHistoryItemDTO {
    @NotNull @NotEmpty
    private RouteDTO route;
    @NotNull @NotEmpty
    private List<String> passengers;
    @NotNull
    private LocalDate date;
    @NotNull
    private Double durationMinutes;
    @NotNull
    private Double cost;
    @NotNull
    private Boolean cancelled;
    private String cancelledBy;
    @NotNull
    private Boolean panic;
    private String panicBy;
    private RatingDTO rating;
    @NotNull @NotEmpty
    private List<String> inconsistencies;

}
