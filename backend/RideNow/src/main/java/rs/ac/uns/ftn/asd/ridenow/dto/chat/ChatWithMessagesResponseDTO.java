package rs.ac.uns.ftn.asd.ridenow.dto.chat;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.MessageDTO;

import java.util.List;

@Getter @Setter
public class ChatWithMessagesResponseDTO {
        private Long id;
        private String user;
        private List<MessageDTO> messages;
}
