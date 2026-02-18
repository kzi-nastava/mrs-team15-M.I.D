package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatWithMessagesResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "Chat", description = "Chat management endpoints")
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Operation(summary = "Get all chats", description = "Admin retrieves all customer support chats")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<ChatResponseDTO>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllChats());
    }

    @Operation(summary = "Open chat for user", description = "User or driver opens a support chat")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    @PostMapping("/user")
    public ResponseEntity<ChatResponseDTO> openChatByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(chatService.openChatByUser(user));
    }

    @Operation(summary = "Mark chat as taken", description = "Admin takes ownership of a chat")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> markChatAsTaken(@PathVariable Long id) {
        chatService.changeTakenStatus(id, true);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Close chat", description = "Admin closes a completed support chat")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/close")
    public ResponseEntity<Void> closeChat(@PathVariable Long id) {
        chatService.changeTakenStatus(id, false);
        return ResponseEntity.ok().build();
    }
}
