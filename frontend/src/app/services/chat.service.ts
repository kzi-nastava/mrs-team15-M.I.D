import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { Chat, Message, WebSocketMessage, SendMessageRequest } from '../model/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = environment.apiUrl + '/chats';
  private wsUrl = environment.backendUrl.replace('http', 'ws') + '/api/chat/websocket';
  private websocket: WebSocket | null = null;
  private messageSubject = new Subject<WebSocketMessage>();
  private connectionSubject = new Subject<boolean>();

  public messages$ = this.messageSubject.asObservable();
  public connectionStatus$ = this.connectionSubject.asObservable();

  constructor(private http: HttpClient) {}

  // REST API Methods

  /**
   * Get all chats (Admin only)
   */
  getAllChats(): Observable<Chat[]> {
    return this.http.get<Chat[]>(`${this.apiUrl}/`);
  }

  /**
   * Mark chat as taken (Admin only)
   */
  markChatAsTaken(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}`, {});
  }

  /**
   * Get user's chat (creates if doesn't exist) (User/Driver only)
*/ getUserChat(): Observable<Chat> { return this.http.post<Chat>(`${this.apiUrl}/user`, {}); } /** * Send message via REST API */


  sendMessageRest(chatId: number, content: string): Observable<Message> {
    const request: SendMessageRequest = { content };
    return this.http.post<Message>(`${this.apiUrl}/message/${chatId}`, request);
  }

  /**
   * Close chat (Admin only) - marks chat as available for other admins
   */
  closeChat(chatId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${chatId}/close`, {});
  }

  // WebSocket Methods

  /**
   * Connect to WebSocket for a specific chat
   */
  connectToChat(chatId: number, token: string): void {
    if (this.websocket) {
      this.disconnect();
    }

    const url = `${this.wsUrl}/${chatId}?token=${token}`;
    console.log('Connecting to WebSocket:', url);
    this.websocket = new WebSocket(url);

    this.websocket.onopen = () => {
      console.log('WebSocket connected to chat:', chatId);
      this.connectionSubject.next(true);
    };

    // Handle incoming WebSocket messages, parse them as JSON, and emit them through
    // the messageSubject for subscribers to consume
    this.websocket.onmessage = (event) => {
      try {
        const message: WebSocketMessage = JSON.parse(event.data);
        console.log('Received WebSocket message:', message);
        this.messageSubject.next(message);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    };

    // Handle WebSocket errors, log them, and emit an error message through the messageSubject
    this.websocket.onerror = (error) => {
      console.error('WebSocket error:', error);
      this.messageSubject.next({ error: 'WebSocket connection error' });
    };

    // Handle WebSocket closure, emit connection status and error message if not closed normally
    this.websocket.onclose = (event) => {
      console.log('WebSocket disconnected. Code:', event.code, 'Reason:', event.reason, 'Clean:', event.wasClean);
      this.websocket = null;
      this.connectionSubject.next(false);
      if (event.code !== 1000) {
        // 1000 is normal closure
        this.messageSubject.next({
          error: `Connection closed: ${event.reason || 'Unknown reason (code: ' + event.code + ')'}`
        });
      }
    };
  }

  /**
   * Send message via WebSocket
   */
  sendMessageWebSocket(content: string): void {
    if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
      const message: SendMessageRequest = { content };
      this.websocket.send(JSON.stringify(message));
    } else {
      console.error('WebSocket is not connected');
    }
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = null;
    }
  }

  /**
   * Check if WebSocket is connected
   */
  isConnected(): boolean {
    return this.websocket !== null && this.websocket.readyState === WebSocket.OPEN;
  }
}
