import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../../services/chat.service';
import { Message, WebSocketMessage } from '../../../model/chat.model';

@Component({
  selector: 'app-admin-chat-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-chat-detail.html',
  styleUrls: ['./admin-chat-detail.css']
})
export class AdminChatDetail implements OnInit, OnDestroy {
  @ViewChild('messageContainer') private messageContainer!: ElementRef;
  chatId: number | null = null;
  messages: Message[] = [];
  newMessage = '';
  loading = true;
  error: string | null = null;
  connected = false;
  private chatClosed = false;

  constructor(
    private chatService: ChatService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.chatId = +params['id'];
      if (this.chatId) {
        this.connectToChat();
      }
    });

    // Subscribe to WebSocket messages
    this.chatService.messages$.subscribe({
      next: (message: WebSocketMessage) => {
        this.handleWebSocketMessage(message);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('WebSocket message error:', err);
        this.error = 'Connection error occurred';
        this.cdr.detectChanges();
      }
    });

    // Subscribe to connection status
    this.chatService.connectionStatus$.subscribe({
      next: (connected: boolean) => {
        this.connected = connected;
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy(): void {
    this.chatService.disconnect();
    // Only close if not already closed (as a fallback)
    if (this.chatId && !this.chatClosed) {
      this.chatService.closeChat(this.chatId).subscribe({
        next: () => console.log('Chat closed successfully'),
        error: (err) => console.error('Error closing chat:', err)
      });
    }
  }

  connectToChat(): void {
    const token = localStorage.getItem('jwtToken');
    if (!token || !this.chatId) {
      this.error = 'Authentication required';
      this.loading = false;
      return;
    }

    // First, mark the chat as taken
    this.chatService.markChatAsTaken(this.chatId).subscribe({
      next: () => {
        // After marking as taken, connect to WebSocket
        this.chatService.connectToChat(this.chatId!, token);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error getting chat:', err);
        this.error = 'Failed to access chat';
        this.loading = false;
      }
    });
  }

  handleWebSocketMessage(wsMessage: WebSocketMessage): void {
    if (wsMessage.error) {
      this.error = wsMessage.error;
      this.connected = false;
      return;
    }

    // Handle different types of WebSocket messages (e.g., existing messages, new message)
    if (wsMessage.type === 'existing_messages' && Array.isArray(wsMessage.data)) {
      this.messages = (wsMessage.data as any[]).map(msg => ({
        content: msg.content,
        userSender: msg.userSender,
        timestamp: this.normalizeTimestamp(msg.timestamp)
      }));
      setTimeout(() => this.scrollToBottom(), 100);
    } else if (wsMessage.type === 'message' && wsMessage.data) {
      const msg = wsMessage.data as any;
      this.messages.push({
        content: msg.content,
        userSender: msg.userSender,
        timestamp: this.normalizeTimestamp(msg.timestamp)
      });
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  // Method to send a new message through the WebSocket connection
  sendMessage(): void {
    if (!this.newMessage.trim() || !this.connected) {
      return;
    }

    this.chatService.sendMessageWebSocket(this.newMessage.trim());
    this.newMessage = '';
  }

  // Method to scroll the message container to the bottom, ensuring the latest messages are visible
  scrollToBottom(): void {
    setTimeout(() => {
      if (this.messageContainer) {
        this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }

  // Method to handle the "Go back" button click, closes the chat if open and navigates back to the admin chats list
  goBack(): void {
    if (this.chatId && !this.chatClosed) {
      this.chatClosed = true;
      this.chatService.closeChat(this.chatId).subscribe({
        next: () => {
          console.log('Chat closed, navigating back');
          this.chatService.disconnect();
          this.router.navigate(['/admin-chats']);
        },
        error: (err) => {
          console.error('Error closing chat:', err);
          // Navigate anyway even if close fails
          this.chatService.disconnect();
          this.router.navigate(['/admin-chats']);
        }
      });
    } else {
      this.chatService.disconnect();
      this.router.navigate(['/admin-chats']);
    }
  }

  // Helper method to normalize timestamps that may come in different formats (e.g., array or ISO string)
  private normalizeTimestamp(timestamp: any): string {
    if (Array.isArray(timestamp)) {
      // Format: [year, month, day, hour, minute, second, nanoseconds]
      const [year, month, day, hour, minute, second] = timestamp;
      // Month is 1-based in the array, need to subtract 1 for JavaScript Date
      const date = new Date(year, month - 1, day, hour, minute, second);
      return date.toISOString();
    }
    return timestamp;
  }
}
