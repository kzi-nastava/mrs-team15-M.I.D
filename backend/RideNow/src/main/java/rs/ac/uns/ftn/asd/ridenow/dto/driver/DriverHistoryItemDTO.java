package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class DriverHistoryItemDTO {
    private RouteDTO route;
    private List<String> passengers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double cost;
    private Boolean cancelled;
    private String cancelledBy;
    private Boolean panic;
    private String panicBy;
    private RatingDTO rating;
    private List<String> inconsistencies;

}
