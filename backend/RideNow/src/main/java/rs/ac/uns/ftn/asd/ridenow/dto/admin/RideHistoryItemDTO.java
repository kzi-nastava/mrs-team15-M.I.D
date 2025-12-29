package rs.ac.uns.ftn.asd.ridenow.dto.admin;

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

    public RideHistoryItemDTO() {
        super();
    }

}