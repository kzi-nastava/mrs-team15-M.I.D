package rs.ac.uns.ftn.asd.ridenow.dto.chat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageRequestDTO {
    @NotNull @NotEmpty
    private String content;
}
