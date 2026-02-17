package rs.ac.uns.ftn.asd.ridenow.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import rs.ac.uns.ftn.asd.ridenow.websocket.ChatWebSocketHandler;
import rs.ac.uns.ftn.asd.ridenow.websocket.NotificationWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    NotificationWebSocketHandler notificationWebSocketHandler;

    private final String allowedOrigin = "http://localhost:4200";

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/api/chat/websocket/{chatId}")
                .setAllowedOrigins(allowedOrigin);
        registry.addHandler(notificationWebSocketHandler, "/api/notifications/websocket")
                .setAllowedOrigins(allowedOrigin);
    }
}
