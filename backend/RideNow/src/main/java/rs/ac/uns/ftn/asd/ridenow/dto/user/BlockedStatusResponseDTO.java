package rs.ac.uns.ftn.asd.ridenow.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BlockedStatusResponseDTO {
    private boolean blocked;
    private String reason;
    private LocalDateTime blockedAt;

    public BlockedStatusResponseDTO() {
        super();
    }

    public BlockedStatusResponseDTO(boolean blocked, String reason, LocalDateTime blockedAt) {
        this.blocked = blocked;
        this.reason = reason;
        this.blockedAt = blockedAt;
    }
}
