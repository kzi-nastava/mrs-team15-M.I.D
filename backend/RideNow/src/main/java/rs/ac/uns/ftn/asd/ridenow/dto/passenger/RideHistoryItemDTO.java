package rs.ac.uns.ftn.asd.ridenow.dto.passenger;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class RideHistoryItemDTO {
    private Long id;
    private String startAddress;
    private String endAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean cancelled;
    private String cancelledBy;
    private double price;
    private boolean panicTriggered;
    private Long routeId;
    private boolean favoriteRoute;

    public RideHistoryItemDTO() {
        super();
    }

}