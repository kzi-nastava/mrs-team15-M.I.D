package rs.ac.uns.ftn.asd.ridenow.dto.model;

import rs.ac.uns.ftn.asd.ridenow.model.Message;

import java.time.LocalDateTime;

public class MessageDTO {
    private String content;
    private Boolean userSender;
    private LocalDateTime timestamp;

    public MessageDTO(Message message){
        this.content = message.getContent();
        this.userSender = message.getUserSender();
        this.timestamp = message.getTimestamp();
    }

    public MessageDTO(){}
}
