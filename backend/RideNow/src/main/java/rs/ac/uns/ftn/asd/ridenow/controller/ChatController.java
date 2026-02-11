package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.ChatWithMessagesResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.MessageRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.chat.MessageResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/")
    public ResponseEntity<List<ChatResponseDTO>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllChats());
    }

    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    @GetMapping("/user")
    public ResponseEntity<ChatWithMessagesResponseDTO> getChatByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(chatService.getChatByUser(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ChatWithMessagesResponseDTO> getChatById(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getChatById(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    @PostMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> sendMessage(@PathVariable Long id, @Valid @RequestBody MessageRequestDTO request){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserRoles role = user.getRole();
        return ResponseEntity.status(202).body(chatService.sendMessage(id, request, role));
    }
}
