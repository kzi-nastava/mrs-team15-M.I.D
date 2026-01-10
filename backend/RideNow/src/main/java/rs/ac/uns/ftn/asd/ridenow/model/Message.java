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
        this.chat = chat;
        this.content = content;
        this.sender = sender;
    }

    public Message() {
    }
}
