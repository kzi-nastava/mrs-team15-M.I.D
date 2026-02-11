import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { Message, WebSocketMessage, Chat } from '../../model/chat.model';

@Component({
  selector: 'app-user-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-chat.html',
  styleUrls: ['./user-chat.css']
})
export class UserChat implements OnInit, OnDestroy {
  @ViewChild('messageContainer') private messageContainer!: ElementRef;
  chatId: number | null = null;
  messages: Message[] = [];
  newMessage = '';
  loading = true;
  error: string | null = null;
  connected = false;

  constructor(
    private chatService: ChatService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.initializeChat();

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
  }

  initializeChat(): void {
    // Get user's chat (creates one if doesn't exist)
    this.chatService.getUserChat().subscribe({
      next: (chat: Chat) => {
        console.log('Got user chat:', chat);
        this.chatId = chat.id;
        this.connectToChat();
      },
      error: (err) => {
        console.error('Error getting user chat:', err);
        this.error = 'Failed to load chat';
        this.loading = false;
      }
    });
  }

  connectToChat(): void {
    const token = localStorage.getItem('jwtToken');
    console.log('Token for WebSocket:', token);
    console.log('Connecting to chat ID:', this.chatId);
    if (!token || !this.chatId) {
      this.error = 'Authentication required';
      this.loading = false;
      return;
    }

    this.chatService.connectToChat(this.chatId, token);
    this.loading = false;
  }

  handleWebSocketMessage(wsMessage: WebSocketMessage): void {
    if (wsMessage.error) {
      this.error = wsMessage.error;
      this.connected = false;
      return;
    }

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

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.connected) {
      return;
    }

    this.chatService.sendMessageWebSocket(this.newMessage.trim());
    this.newMessage = '';
  }

  scrollToBottom(): void {
    setTimeout(() => {
      if (this.messageContainer) {
        this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }

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
