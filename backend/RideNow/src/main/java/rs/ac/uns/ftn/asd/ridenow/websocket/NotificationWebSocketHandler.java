package rs.ac.uns.ftn.asd.ridenow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import rs.ac.uns.ftn.asd.ridenow.dto.websocket.WebSocketMessageDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.security.JwtUtil;
import rs.ac.uns.ftn.asd.ridenow.service.PanicAlertService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PanicAlertService panicAlertService;

    private final ObjectMapper objectMapper;

    // For admin panic alerts
    private final Map<Long, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

    // For all user notifications (including passengers, drivers, and admins)
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final Map<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();

    // For current ride - maps ride to list of drivers and passengers
    private final Map<Long, Set<Long>> rideParticipants = new ConcurrentHashMap<>();

    public NotificationWebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractTokenFromQuery(session);

        if (token == null) {
            System.err.println("DEBUG: No token provided in WebSocket connection");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("No token provided"));
            return;
        }

        User user = authenticateUser(token);
        if (user == null) {
            System.err.println("DEBUG: Authentication failed for token");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication failed"));
            return;
        }

        // Store user session for all notification types
        userSessions.put(user.getId(), session);
        sessionIdToUserId.put(session.getId(), user.getId());
        System.out.println("DEBUG: User " + user.getId() + " (" + user.getEmail() + ") connected to notifications WebSocket");
        System.out.println("DEBUG: Current sessions: " + userSessions.keySet());

        // If user is admin, also store in admin sessions for panic alerts
        if ("ADMIN".equals(user.getRole().name())) {
            adminSessions.put(user.getId(), session);
            System.out.println("DEBUG: User " + user.getId() + " is an admin, added to admin sessions");
            // Send current unresolved panic alerts to newly connected admin
            try {
                sendInitialState(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming messages if needed - for now just echo back
        if (message instanceof TextMessage textMessage) {
            try {
                // Parse and echo back for debugging
                String payload = textMessage.getPayload();
                System.out.println("Received message: " + payload);

                // Echo back for testing
                session.sendMessage(new TextMessage("{\"type\":\"echo\",\"data\":\"Message received\"}"));
            } catch (Exception e) {
                System.err.println("Error handling message: " + e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        exception.printStackTrace();
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
            System.err.println("ERROR: Exception during token validation: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String extractTokenFromQuery(WebSocketSession session) {
        URI uri = session.getUri();

        if (uri == null) {
            return null;
        }

        String query = uri.getQuery();

        if (query == null) {
            return null;
        }

        String[] params = query.split("&");

        for (int i = 0; i < params.length; i++) {
            String param = params[i];

            if (param.startsWith("token=")) {
                String token = param.substring(6);
                return token;
            }
        }

        return null;
    }

    private void sendInitialState(WebSocketSession session) {
        try{
            WebSocketMessageDTO message = new WebSocketMessageDTO("INITIAL_STATE", panicAlertService.getAllUnresolvedAlerts());
            String payload = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
        } catch (Exception e) {
            System.err.println("Failed to send initial state: " + e.getMessage());
        }
    }

    private void cleanupSession(WebSocketSession session) {
        Long userId = sessionIdToUserId.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
            adminSessions.remove(userId); // Remove from admin sessions too if it was there
        }
        rideParticipants.values().forEach(participants -> participants.remove((userId)));
    }

    // Broadcast to specific user (new method for user notifications)
    public void broadcastToUser(Long userId, String action, Object data) {
        WebSocketSession session = userSessions.get(userId);
        if (session == null) {
            System.err.println("DEBUG: No session found for userId " + userId + ". Available sessions: " + userSessions.keySet());
            return;
        }

        if (!session.isOpen()) {
            System.err.println("DEBUG: Session for userId " + userId + " is closed");
            userSessions.remove(userId);
            return;
        }

        try {
            WebSocketMessageDTO message = new WebSocketMessageDTO(action, data);
            String payload = objectMapper.writeValueAsString(message);
            System.out.println("DEBUG: Broadcasting " + action + " to user " + userId);
            session.sendMessage(new TextMessage(payload));
            System.out.println("DEBUG: Message sent successfully to user " + userId);
        } catch (Exception e) {
            System.err.println("DEBUG: Failed to send message to user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            // Remove broken session
            userSessions.remove(userId);
            sessionIdToUserId.values().removeIf(id -> id.equals(userId));
        }
    }

    // Broadcast new panic alert (existing method for admins)
    public void broadcastNewPanic(Object panicAlertDTO) {
        WebSocketMessageDTO message = new WebSocketMessageDTO("NEW_PANIC", panicAlertDTO);
        broadcastToAdmins(message);
    }

    // Broadcast panic resolution (existing method for admins)
    public void broadcastPanicResolved(Long panicAlertId) {
        WebSocketMessageDTO message = new WebSocketMessageDTO("PANIC_RESOLVED", panicAlertId);
        broadcastToAdmins(message);
    }

    private void broadcastToAdmins(WebSocketMessageDTO message) {
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
                    System.err.println("Failed to send message to admin: " + e.getMessage());
                    return true; // Remove failed session
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to broadcast to admins: " + e.getMessage());
        }
    }

    public void registerRideParticipant(Long rideId, Long userId){
        rideParticipants.computeIfAbsent(rideId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        System.out.println("Registered user" + userId + "for ride" + rideId);
    }

    public void registerRideParticipants(Long rideId, List<Long> userIds){
        Set<Long> participants = rideParticipants.computeIfAbsent(rideId, k -> ConcurrentHashMap.newKeySet());
        participants.addAll(userIds);
        System.out.println("Registered " + userIds.size() + " participants for ride " + rideId);
    }

    public void unregisterRide(Long rideId) {
        rideParticipants.remove(rideId);
        System.out.println("Unregistered all participants from ride " + rideId);
    }

    public void broadcastRidePanic(Long rideId, Object panicData) {
        broadcastToRide(rideId, "RIDE_PANIC", panicData);
    }

    public void broadcastRideStop(Long rideId, Object stopData) {
        broadcastToRide(rideId, "RIDE_STOPPED", stopData);
    }

    public void broadcastRideComplete(Long rideId, Object completionData) {
        broadcastToRide(rideId, "RIDE_COMPLETED", completionData);
    }

    private void broadcastToRide(Long rideId, String action, Object data) {
        Set<Long> participants = rideParticipants.get(rideId);

        if (participants == null || participants.isEmpty()) {
            System.out.println("No participants registered for ride " + rideId);
            return;
        }

        WebSocketMessageDTO message = new WebSocketMessageDTO(action, data);

        try {
            String payload = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(payload);

            int successCount = 0;
            for (Long userId : participants) {
                WebSocketSession session = userSessions.get(userId);
                if (session != null && session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                        successCount++;
                    } catch (Exception e) {
                        System.err.println("Failed to send to user " + userId + ": " + e.getMessage());
                    }
                }
            }
            System.out.println("Broadcast " + action + " to " + successCount + "/" + participants.size() + " participants for ride " + rideId);
        } catch (Exception e) {
            System.err.println("Failed to broadcast to ride " + rideId + ": " + e.getMessage());
        }
    }
}