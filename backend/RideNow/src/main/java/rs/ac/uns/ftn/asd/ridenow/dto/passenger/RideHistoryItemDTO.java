package rs.ac.uns.ftn.asd.ridenow.dto.passenger;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class RideHistoryItemDTO {
    private Long rideId;
    private RouteDTO route;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean cancelled;
    private String cancelledBy;
    private Double price;
    private Boolean panic;
    private String panicBy;
    private Long routeId;
    private boolean favoriteRoute;
    private List<String> passengers;
    private RatingDTO rating;
    private List<String> inconsistencies;
    private String driver;

    public RideHistoryItemDTO() {
        super();
    }
}