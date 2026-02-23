import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

export interface WebSocketMessage {
  action: string;
  data: any;
}

@Injectable({
  providedIn: 'root'
})
export class SharedWebSocketService {
  private socket: WebSocket | null = null;
  private messageSubject = new Subject<WebSocketMessage>();
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);

  public message$: Observable<WebSocketMessage> = this.messageSubject.asObservable();
  public connectionStatus$: Observable<boolean> = this.connectionStatusSubject.asObservable();

  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private isConnecting = false;
  private shouldReconnect = true;

  constructor(private ngZone: NgZone) {}

  /**
   * Establish a single WebSocket connection to the backend
   * All services will use this shared connection
   */
  connect(): void {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      console.error('No auth token found');
      return;
    }

    if (this.isConnecting || (this.socket && this.socket.readyState === WebSocket.OPEN)) {
      return;
    }

    this.isConnecting = true;
    this.shouldReconnect = true;

    try {
      // Construct the WebSocket URL with the token as a query parameter for authentication
      const wsUrl = `ws://localhost:8081/api/notifications/websocket?token=${encodeURIComponent(token)}`;
      console.log('Establishing shared WebSocket connection...');

      this.socket = new WebSocket(wsUrl);

      // Handle WebSocket open event, emit a connection status update, and reset reconnection attempts
      this.socket.onopen = () => {
        this.ngZone.run(() => {
          console.log('Shared WebSocket connected');
          this.connectionStatusSubject.next(true);
          this.reconnectAttempts = 0;
          this.isConnecting = false;
        });
      };

      // Handle incoming WebSocket messages, parse them as JSON, and emit them through the messageSubject for subscribers to consume
      this.socket.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          console.log('Shared WebSocket message:', message);

          // Broadcast this message to all subscribers
          this.ngZone.run(() => {
            this.messageSubject.next(message);
          });
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      };

      // Handle WebSocket errors, emit a connection status update, and log the error
      this.socket.onerror = (error) => {
        console.error('Shared WebSocket error:', error);
        this.connectionStatusSubject.next(false);
        this.isConnecting = false;
      };

      // Handle WebSocket closure, emit a connection status update, and if the closure was not normal, emit an error message through the messageSubject for subscribers to consume
      this.socket.onclose = (event) => {
        console.log('Shared WebSocket closed');
        this.connectionStatusSubject.next(false);
        this.isConnecting = false;
        this.socket = null;

        if (this.shouldReconnect && event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          const delay = this.reconnectDelay * this.reconnectAttempts;
          console.log(`Reconnecting in ${delay}ms...`);
          setTimeout(() => this.connect(), delay);
        }
      };

    } catch (error) {
      console.error('Failed to create WebSocket:', error);
      this.connectionStatusSubject.next(false);
      this.isConnecting = false;
    }
  }

  // Method to disconnect from the WebSocket, closes the connection and updates the connection status, also prevents automatic reconnection attempts
  disconnect(): void {
    this.shouldReconnect = false;
    this.reconnectAttempts = 0;

    if (this.socket) {
      console.log('Disconnecting shared WebSocket...');
      try {
        this.socket.close(1000, 'Client disconnect');
      } catch (error) {
        console.error('Error closing WebSocket:', error);
      }
      this.socket = null;
      this.connectionStatusSubject.next(false);
    }

    this.isConnecting = false;
  }

  isConnected(): boolean {
    return this.socket !== null && this.socket.readyState === WebSocket.OPEN;
  }

  reconnect(): void {
    console.log('Manual reconnection requested');
    this.disconnect();
    setTimeout(() => this.connect(), 500);
  }
}
