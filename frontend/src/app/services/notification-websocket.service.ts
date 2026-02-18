import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Ride } from '../history/components/ride-history-table/ride-history-table';
import { CurrentRide } from '../ride/pages/current-ride/current-ride';
import { SharedWebSocketService } from './shared-websocket.service';

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
  action: 'NEW_PANIC' | 'PANIC_RESOLVED' | 'INITIAL_STATE' | 'RIDE_PANIC' | 'RIDE_STOPPED' | 'RIDE_COMPLETED';
  data: any;
}

export interface RideEventData {
  rideId: number;
  triggeredBy?: string;
  triggeredByUserId?: number;
  endAddress?: string;
  distanceKm?: number;
  estimatedDurationMin?: number;
  price?: number;
  route?: any;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationWebSocketService {
  // Store all unresolved alerts
  private unresolvedAlertsSubject = new BehaviorSubject<PanicAlert[]>([]);
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);

  public unresolvedAlerts$: Observable<PanicAlert[]> = this.unresolvedAlertsSubject.asObservable();
  public connectionStatus$: Observable<boolean> = this.connectionStatusSubject.asObservable();

  private ridePanicSubject = new BehaviorSubject<RideEventData | null>(null);
  private rideStoppedSubject = new BehaviorSubject<RideEventData | null>(null);
  private rideCompletedSubject = new BehaviorSubject<RideEventData | null>(null);

  public ridePanic$: Observable<RideEventData | null> = this.ridePanicSubject.asObservable();
  public rideStopped$: Observable<RideEventData | null> = this.rideStoppedSubject.asObservable();
  public rideCompleted$: Observable<RideEventData | null> = this.rideCompletedSubject.asObservable();

  private audioContext: AudioContext | null = null;
  private messageSubscription: any = null;

  constructor(private ngZone: NgZone, private sharedWebSocket: SharedWebSocketService) {}

  connect(): void {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      console.error('No auth token found');
      return;
    }

    // If already subscribed, don't subscribe again
    if (this.messageSubscription) {
      return;
    }

    console.log('Connecting to shared WebSocket for panic alerts...');

    // Connect the shared WebSocket
    this.sharedWebSocket.connect();

    // Subscribe to messages from the shared WebSocket
    this.messageSubscription = this.sharedWebSocket.message$.subscribe(
      (message: any) => {
        this.handleWebSocketMessage(message);
        this.connectionStatusSubject.next(true);
      },
      (error: any) => {
        console.error('Error in WebSocket message stream:', error);
        this.connectionStatusSubject.next(false);
      },
      () => {
        console.log('WebSocket message stream completed');
        this.connectionStatusSubject.next(false);
      }
    );

    // Monitor connection status
    this.sharedWebSocket.connectionStatus$.subscribe(
      (connected: boolean) => {
        if (!connected) {
          this.connectionStatusSubject.next(false);
        }
      }
    );
  }

  private handleWebSocketMessage(message: WebSocketMessage): void {
    this.ngZone.run(() => {
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
        case 'RIDE_COMPLETED':
          console.log('RIDE COMPLETED EVENT:', message.data);
          this.rideCompletedSubject.next(message.data as RideEventData)
          break
      }
    });
  }

  disconnect(): void {
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
      this.messageSubscription = null;
    }

    console.log('Disconnecting from shared WebSocket...');
    this.sharedWebSocket.disconnect();

    this.connectionStatusSubject.next(false);
    this.unresolvedAlertsSubject.next([]);

    this.ridePanicSubject.next(null);
    this.rideStoppedSubject.next(null);
    this.rideCompletedSubject.next(null);
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
    return this.sharedWebSocket.isConnected();
  }

  reconnect(): void {
    console.log('Manual reconnection requested');
    this.disconnect();
    setTimeout(() => this.connect(), 500);
  }
}
