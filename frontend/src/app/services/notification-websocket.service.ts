import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Ride } from '../history/components/ride-history-table/ride-history-table';

export interface PanicAlert {
  id: number;
  rideId: number;
  panicBy: string;
  panicByRole: string;
  createdAt: string;
  resolved: boolean;
  resolvedAt?: string;
  resolvedBy?: number;
  resolvedByEmail?: string;
  driverEmail?: string;
  passengerEmail?: string;
  location?: {
    lat: number;
    lng: number;
  };
}

export interface WebSocketMessage {
  action: 'NEW_PANIC' | 'PANIC_RESOLVED' | 'INITIAL_STATE' | 'RIDE_PANIC' | 'RIDE_STOPPED' | 'ROUTE_UPDATED' | 'RIDE_COMPLETED';
  data: any;
}

export interface RideEventData {
  rideId: number;
  triggeredBy?: string; 
  triggeredByUserId?: number;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationWebSocketService {
  private socket: WebSocket | null = null;
  
  // Store all unresolved alerts
  private unresolvedAlertsSubject = new BehaviorSubject<PanicAlert[]>([]);
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  
  public unresolvedAlerts$: Observable<PanicAlert[]> = this.unresolvedAlertsSubject.asObservable();
  public connectionStatus$: Observable<boolean> = this.connectionStatusSubject.asObservable();

  private ridePanicSubject = new BehaviorSubject<RideEventData | null>(null);
  private rideStoppedSubject = new BehaviorSubject<RideEventData | null>(null);
  private routeUpdatedSubject = new BehaviorSubject<RideEventData | null>(null);
  private rideCompletedSubject = new BehaviorSubject<RideEventData | null>(null);

  public ridePanic$: Observable<RideEventData | null> = this.ridePanicSubject.asObservable();
  public rideStopped$: Observable<RideEventData | null> = this.rideStoppedSubject.asObservable();
  public routeUpdated$: Observable<RideEventData | null> = this.routeUpdatedSubject.asObservable();
  public rideCompleted$: Observable<RideEventData | null> = this.rideCompletedSubject.asObservable();

  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private audioContext: AudioContext | null = null;
  private isConnecting = false;
  private shouldReconnect = true;

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
      const wsUrl = `ws://localhost:8081/api/notifications/websocket?token=${encodeURIComponent(token)}`;
      console.log('Connecting to WebSocket...');
      
      this.socket = new WebSocket(wsUrl);

      this.socket.onopen = () => {
        console.log('WebSocket connected');
        this.connectionStatusSubject.next(true);
        this.reconnectAttempts = 0;
        this.isConnecting = false;
      };

      this.socket.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          console.log('WebSocket message:', message);
          this.handleWebSocketMessage(message);
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      };

      this.socket.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.connectionStatusSubject.next(false);
        this.isConnecting = false;
      };

      this.socket.onclose = (event) => {
        console.log('WebSocket closed');
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

  private handleWebSocketMessage(message: WebSocketMessage): void {
    const currentAlerts = this.unresolvedAlertsSubject.value;

    switch (message.action) {
      case 'INITIAL_STATE':
        // Set initial unresolved alerts
        const alerts = message.data as PanicAlert[];
        console.log(`Received ${alerts.length} unresolved alerts`);
        this.unresolvedAlertsSubject.next(alerts);
        break;

      case 'NEW_PANIC':
        // Add new panic alert
        const newAlert = message.data as PanicAlert;
        console.log('NEW PANIC ALERT:', newAlert);
        this.unresolvedAlertsSubject.next([newAlert, ...currentAlerts]);
        this.playAlertSound();
        break;

      case 'PANIC_RESOLVED':
        const resolvedId = message.data as number;
        console.log('Panic resolved:', resolvedId);
        const updatedAlerts = currentAlerts.filter(alert => alert.id !== resolvedId);
        this.unresolvedAlertsSubject.next(updatedAlerts);
        break;

      case 'RIDE_PANIC':
        console.log('RIDE PANIC EVENT:', message.data);
        this.ridePanicSubject.next(message.data as RideEventData)
        this.playAlertSound();
        break
      case 'RIDE_STOPPED':
        console.log('RIDE STOPPED EVENT:', message.data);
        this.rideStoppedSubject.next(message.data as RideEventData)
        break
      case 'ROUTE_UPDATED':
        console.log('ROUTE UPDATED EVENT:', message.data);
        this.routeUpdatedSubject.next(message.data as RideEventData)
        break
      case 'RIDE_COMPLETED':
        console.log('RIDE COMPLETED EVENT:', message.data);
        this.rideCompletedSubject.next(message.data as RideEventData)
        break
    }
  }

  disconnect(): void {
    this.shouldReconnect = false;
    this.reconnectAttempts = 0;
    
    if (this.socket) {
      console.log('Disconnecting WebSocket...');
      try {
        this.socket.close(1000, 'Client disconnect');
      } catch (error) {
        console.error('Error closing WebSocket:', error);
      }
      this.socket = null;
      this.connectionStatusSubject.next(false);
    }
    this.unresolvedAlertsSubject.next([]);

    this.ridePanicSubject.next(null);
    this.rideStoppedSubject.next(null);
    this.routeUpdatedSubject.next(null);
    this.rideCompletedSubject.next(null);
    
    this.isConnecting = false;
  }

  private playAlertSound(): void {
    try {
      if (!this.audioContext) {
        this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      }
      
      console.log('Playing alert sound');
      this.createBeep(800, 0.3, 0.5);
      setTimeout(() => this.createBeep(1000, 0.3, 0.5), 600);
      setTimeout(() => this.createBeep(1200, 0.3, 0.5), 1200);
    } catch (error) {
      console.error('Error playing sound:', error);
    }
  }

  private createBeep(frequency: number, volume: number, duration: number): void {
    if (!this.audioContext) return;

    const oscillator = this.audioContext.createOscillator();
    const gainNode = this.audioContext.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(this.audioContext.destination);

    oscillator.frequency.value = frequency;
    oscillator.type = 'sine';

    gainNode.gain.setValueAtTime(volume, this.audioContext.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + duration);

    oscillator.start(this.audioContext.currentTime);
    oscillator.stop(this.audioContext.currentTime + duration);
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