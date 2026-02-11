package rs.ac.uns.ftn.asd.ridenow.service;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatWithMessagesResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.MessageRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.MessageResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.MessageDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Chat;
import rs.ac.uns.ftn.asd.ridenow.model.Message;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.ChatRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    public List<ChatResponseDTO> getAllChats(){
            return chatRepository.findAll().stream().map(chat -> {
                ChatResponseDTO dto = new ChatResponseDTO();
                dto.setId(chat.getId());
                dto.setUser(chat.getUser().getFirstName() + " " + chat.getUser().getLastName());
                return dto;
            }).toList();
    }

    public ChatWithMessagesResponseDTO getChatByUser(User user){
        Optional<Chat> chatOpt = chatRepository.findByUser(user);
        Chat chat;
        chat = chatOpt.orElseGet(() -> createChat(user));
        return makeChatResponse(chat);
    }

    public ChatWithMessagesResponseDTO getChatById(Long id){
        Optional<Chat> chatOpt = chatRepository.findById(id);
        if(chatOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat not found with id: " + id);
        }
        return makeChatResponse(chatOpt.get());
    }

    @NonNull
    private ChatWithMessagesResponseDTO makeChatResponse(Chat chat) {
        ChatWithMessagesResponseDTO response = new ChatWithMessagesResponseDTO();
        response.setId(chat.getId());
        response.setUser(chat.getUser().getFirstName() + " " + chat.getUser().getLastName());
        response.setMessages(chat.getMessages().stream().map(MessageDTO::new).toList());
        return response;
    }

    private Chat createChat(User user){
        Chat chat = new Chat();
        chat.setUser(user);
        chat.setTaken(false);
        chat.setMessages(new ArrayList<Message>());
        return chatRepository.save(chat);
    }

    public MessageResponseDTO sendMessage(Long chatId, MessageRequestDTO request, UserRoles role){
        Message message = new Message();
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()){
            throw new IllegalArgumentException("Chat not found with id: " + chatId);
        }
        message.setChat(chatOpt.get());
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setUserSender(role != UserRoles.ADMIN);
        Message savedMessage = messageRepository.save(message);
        MessageResponseDTO response = new MessageResponseDTO();
        response.setContent(savedMessage.getContent());
        response.setId(savedMessage.getId());
        return response;
    }
}
