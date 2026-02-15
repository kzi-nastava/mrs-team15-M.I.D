package rs.ac.uns.ftn.asd.ridenow.dto.websocket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketMessageDTO {
    private String action;
    private Object data;

    public WebSocketMessageDTO(String action, Object data) {
        this.action = action;   // NEW_PANIC, PANIC_RESOLVED, INITIAL_STATE
        this.data = data;
    }
}