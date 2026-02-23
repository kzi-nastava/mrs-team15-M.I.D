package rs.ac.uns.ftn.asd.ridenow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatWithMessagesResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.MessageDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Chat;
import rs.ac.uns.ftn.asd.ridenow.model.Message;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.ChatRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.MessageRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository,
            UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<ChatResponseDTO> getAllChats() {
        return chatRepository.findNotTaken().stream().map(chat -> {
            ChatResponseDTO dto = new ChatResponseDTO();
            dto.setId(chat.getId());
            dto.setUser(chat.getUser().getFirstName() + " " + chat.getUser().getLastName());

            // Check if the last message was sent by the user (userSender = true)
            boolean hasNewMessages = !chat.getMessages().isEmpty() &&
                    chat.getMessages().get(chat.getMessages().size() - 1).getUserSender();
            dto.setHasNewMessages(hasNewMessages);

            return dto;
        }).toList();
    }

    public ChatResponseDTO openChatByUser(User user) {
        // If a chat already exists for this user, return it. Otherwise, create a new chat.
        Optional<Chat> chatOpt = chatRepository.findByUser(user);
        Chat chat;
        chat = chatOpt.orElseGet(() -> createChat(user));
        ChatResponseDTO response = new ChatResponseDTO();
        response.setId(chat.getId());
        response.setUser(chat.getUser().getFirstName() + " " + chat.getUser().getLastName());
        return response;
    }

    private Chat createChat(User user) {
        Chat chat = new Chat();
        chat.setUser(user);
        chat.setTaken(false);
        chat.setMessages(new ArrayList<>());
        return chatRepository.save(chat);
    }

    public void changeTakenStatus(Long chatId, Boolean taken) {
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat not found with id: " + chatId);
        }
        Chat chat = chatOpt.get();
        chat.setTaken(taken);
        chatRepository.save(chat);
    }

    // WebSocket-specific methods
    public MessageDTO sendMessageViaWebSocket(Long chatId, String content, Long userId) {
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat not found with id: " + chatId);
        }

        Chat chat = chatOpt.get();

        // Verify user has access to this chat (either the chat owner or an admin)
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        User user = userOpt.get();
        boolean isAdmin = user.getRole() == UserRoles.ADMIN;
        boolean isOwner = chat.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("Access denied to chat " + chatId);
        }

        Message message = new Message();
        message.setChat(chat);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setUserSender(!isAdmin); // True if user is not admin

        Message savedMessage = messageRepository.save(message);
        return new MessageDTO(savedMessage);
    }

    @Transactional(readOnly = true)
    public void sendExistingMessagesToWebSocket(Long chatId, WebSocketSession session) {
        try {
            Optional<Chat> chatOpt = chatRepository.findById(chatId);
            if (chatOpt.isEmpty()) {
                throw new IllegalArgumentException("Chat not found with id: " + chatId);
            }

            Chat chat = chatOpt.get();
            List<MessageDTO> messages = chat.getMessages().stream()
                    .map(MessageDTO::new)
                    .toList();

            String messagesJson = objectMapper.writeValueAsString(Map.of(
                    "type", "existing_messages",
                    "data", messages));

            session.sendMessage(new TextMessage(messagesJson));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send existing messages", e);
        }
    }

    public Chat validateChatAccess(Long chatId, Long userId) {
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat not found with id: " + chatId);
        }

        Chat chat = chatOpt.get();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        User user = userOpt.get();
        boolean isAdmin = user.getRole() == UserRoles.ADMIN;
        boolean isOwner = chat.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("Access denied to chat " + chatId);
        }

        return chat;
    }
}
