package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "chat_id", nullable = false)
    Chat chat;

    @Column(nullable = false)
    private Boolean userSender;

    public Message(String content, Chat chat, Boolean userSender) {
        this.content = content;
        this.userSender = userSender;
        this.assignChat(chat);
    }

    public Message() {}

    public  void assignChat(Chat chat) {
        this.chat = chat;
        if(chat != null && !chat.getMessages().contains(this)){
            chat.addMessage(this);
        }
    }
}
