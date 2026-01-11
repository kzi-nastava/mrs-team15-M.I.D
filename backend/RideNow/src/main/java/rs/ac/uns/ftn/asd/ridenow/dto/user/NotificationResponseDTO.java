package rs.ac.uns.ftn.asd.ridenow.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class NotificationResponseDTO {
    @NotNull @NotEmpty
    private String message;
    @NotNull
    private LocalDateTime timestamp;
}
