package rs.ac.uns.ftn.asd.ridenow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import rs.ac.uns.ftn.asd.ridenow.dto.websocket.WebSocketMessageDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.security.JwtUtil;
import rs.ac.uns.ftn.asd.ridenow.service.PanicAlertService;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PanicAlertService panicAlertService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, WebSocketSession> adminSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractTokenFromQuery(session);
        User user = authenticateUser(token);

        if (user == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication failed"));
            return;
        }

        if (!"ADMIN".equals(user.getRole().name())) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Admin access only"));
            return;
        }

        adminSessions.put(user.getId(), session);
        sessionIdToUserId.put(session.getId(), user.getId());

        System.out.println("Admin connected: " + user.getEmail() + " (Total: " + adminSessions.size() + ")");

        // Send current unresolved panic alerts to newly connected admin
        sendInitialState(session);

    }


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming messages if needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket error: " + exception.getMessage());
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        cleanupSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private User authenticateUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                return null;
            }
            String email = jwtUtil.extractEmail(token);
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.isJwtTokenValid()) {
                    return user;
                }
            }
        } catch (Exception e) {
            System.err.println("Token validation error: " + e.getMessage());
        }
        return null;
    }

    private String extractTokenFromQuery(WebSocketSession session) {
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

    private void sendInitialState(WebSocketSession session) {
        try{
            WebSocketMessageDTO message = new WebSocketMessageDTO("INITIAL_STATE", panicAlertService.getAllUnresolvedAlerts());
            String payload = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
            System.out.println("Sent initial state to admin");
        } catch (Exception e) {
            System.err.println("Failed to send initial state: " + e.getMessage());
        }
    }

    private void cleanupSession(WebSocketSession session) {
        Long userId = sessionIdToUserId.remove(session.getId());
        if (userId != null) {
            adminSessions.remove(userId);
            System.out.println("Admin disconnected. Remaining: " + adminSessions.size());
        }
    }

    // Broadcast new panic alert
    public void broadcastNewPanic(Object panicAlertDTO) {
        WebSocketMessageDTO message = new WebSocketMessageDTO("NEW_PANIC", panicAlertDTO);
        broadcastMessage(message);
    }

    // Broadcast panic resolution
    public void broadcastPanicResolved(Long panicAlertId) {
        WebSocketMessageDTO message = new WebSocketMessageDTO("PANIC_RESOLVED", panicAlertId);
        broadcastMessage(message);
    }

    private void broadcastMessage(WebSocketMessageDTO message) {
        if (adminSessions.isEmpty()) {
            System.out.println("No admins connected - message not sent");
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(payload);

            adminSessions.values().removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                        return false; // Keep session
                    }
                    return true; // Remove closed session
                } catch (Exception e) {
                    System.err.println("Failed to send message: " + e.getMessage());
                    return true; // Remove failed session
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to broadcast: " + e.getMessage());
        }
    }

    public int getConnectedAdminCount() {
        return adminSessions.size();
    }
}