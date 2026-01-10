package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Message> messages = new ArrayList<>();

    public Chat(List<Message> messages) {
        this.messages = messages;
        for(Message message : messages){
            message.setChat(this);
        }
    }

    public Chat() {

    }

    public void addMessage(Message message){
        if (!messages.contains(message)){
            messages.add(message);
            message.setChat(this);
        }
    }

    public void removeMessage(Message message){
        if (messages.contains(message)){
            messages.remove(message);
            message.setChat(null);
        }
    }
}
