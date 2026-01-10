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

    @Column(nullable = false)
    @Lob
    private String content;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name= "chat_id", nullable = false)
    Chat chat;

    @ManyToOne
    @JoinColumn (name = "user_id", nullable = false)
    User sender;

    public Message(String content, Chat chat, User sender) {
        this.content = content;
        this.sender = sender;
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
