package rs.ac.uns.ftn.asd.ridenow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import rs.ac.uns.ftn.asd.ridenow.dto.model.MessageDTO;
import rs.ac.uns.ftn.asd.ridenow.security.JwtUtil;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.service.ChatService;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Map to store active sessions: chatId -> Set of sessions
    private final Map<Long, Map<String, WebSocketSession>> chatSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractTokenFromQuery(session);
        Long chatId = extractChatIdFromPath(session);
        User user = authenticateUser(token);

        if (user == null || chatId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication failed or invalid chat ID"));
            return;
        }

        // Store user information in session attributes
        session.getAttributes().put("userId", user.getId());
        session.getAttributes().put("userRole", user.getRole());
        session.getAttributes().put("chatId", chatId);

        try {
            // Validate chat access
            chatService.validateChatAccess(chatId, user.getId());

            // Add session to chat room
            chatSessions.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                    .put(session.getId(), session);

            // Send existing messages to the newly connected user
            chatService.sendExistingMessagesToWebSocket(chatId, session);
        } catch (Exception e) {
            // If chat doesn't exist or user doesn't have access, close connection
            System.err.println("WebSocket connection error: " + e.getMessage());
            e.printStackTrace();
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Access denied"));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            try {
                // Parse incoming message
                WebSocketMessageRequest messageRequest = objectMapper.readValue(textMessage.getPayload(),
                        WebSocketMessageRequest.class);

                Long chatId = (Long) session.getAttributes().get("chatId");
                Long userId = (Long) session.getAttributes().get("userId");

                // Send message through chat service
                MessageDTO savedMessage = chatService.sendMessageViaWebSocket(chatId, messageRequest.getContent(),
                        userId);

                // Broadcast message to all clients in this chat
                broadcastMessageToChat(chatId, savedMessage);

            } catch (Exception e) {
                // Send error message back to sender
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        Map.of("error", "Failed to send message: " + e.getMessage()))));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Clean up session
        removeSessionFromChat(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        removeSessionFromChat(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String extractTokenFromQuery(WebSocketSession session) {
        // Extract token from query parameters
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String[] params = uri.getQuery().split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        return null;
    }

    private Long extractChatIdFromPath(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String path = uri.getPath();
                String[] segments = path.split("/");
                // Path should be like /api/chat/websocket/{chatId}
                if (segments.length >= 5) {
                    return Long.parseLong(segments[4]);
                }
            }
        } catch (NumberFormatException e) {
            // Invalid chat ID format
        }
        return null;
    }

    private User authenticateUser(String token) {
        // Validate token and retrieve user
        if (token == null || !jwtUtil.validateToken(token)) {
            return null;
        }

        // Extract email from token and find user
        String email = jwtUtil.extractEmail(token);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isJwtTokenValid()) {
                return user;
            }
        }
        return null;
    }

    private void removeSessionFromChat(WebSocketSession session) {
        // Remove session from chat room
        Long chatId = (Long) session.getAttributes().get("chatId");
        if (chatId != null) {
            Map<String, WebSocketSession> sessions = chatSessions.get(chatId);
            if (sessions != null) {
                sessions.remove(session.getId());
                if (sessions.isEmpty()) {
                    chatSessions.remove(chatId);
                }
            }
        }
    }

    public void broadcastMessageToChat(Long chatId, MessageDTO message) {
        // Get all sessions for this chat
        Map<String, WebSocketSession> sessions = chatSessions.get(chatId);
        System.out.println(
                "Broadcasting message to chat " + chatId + ". Sessions: " + (sessions != null ? sessions.size() : 0));
        if (sessions != null) {
            String messageJson;
            try {
                messageJson = objectMapper.writeValueAsString(Map.of(
                        "type", "message",
                        "data", message));
                System.out.println("Broadcasting: " + messageJson);
            } catch (Exception e) {
                System.err.println("Failed to serialize message: " + e.getMessage());
                return; // Failed to serialize message
            }

            sessions.values().removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(messageJson));
                        return false; // Keep session
                    }
                } catch (Exception e) {
                    // Session is broken, remove it
                }
                return true; // Remove session
            });
        }
    }

    // Inner class for WebSocket message requests
    public static class WebSocketMessageRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
