package rs.ac.uns.ftn.asd.ridenow.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class MessageDTO {
    private String message;
    private Long senderId;
    private LocalDateTime timestamp;
}
