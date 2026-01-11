package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.MessageDTO;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    @GetMapping("/{id}")
    public ResponseEntity<List<MessageDTO>> getChatById(@RequestParam @NotNull Long id) {
        MessageDTO message1 = new MessageDTO();
        message1.setSenderId(1L);
        message1.setMessage("Hello, how are you?");
        message1.setTimestamp(LocalDateTime.now());
        MessageDTO message2 = new MessageDTO();
        message2.setSenderId(2L);
        message2.setMessage("I'm fine, thank you!");
        message2.setTimestamp(LocalDateTime.now());
        List<MessageDTO> messages = List.of(message1, message2);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id")
    public ResponseEntity<MessageDTO> sendMessage(@PathVariable @NotNull Long id, @RequestBody @NotNull MessageDTO messageRequest) {
        MessageDTO messageResponse = new MessageDTO();
        messageResponse.setSenderId(messageRequest.getSenderId());
        messageResponse.setMessage(messageRequest.getMessage());
        messageResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(201).body(messageResponse);
    }

    @PostMapping("/start")
    public ResponseEntity<Long> startChat(@RequestParam @NotNull Long userId1, @RequestParam @NotNull Long userId2) {
        Long chatId = 1001L; // Example chat ID
        return ResponseEntity.status(201).body(chatId);
    }
}
