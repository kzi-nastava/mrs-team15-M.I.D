import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable, filter, take } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { SharedWebSocketService } from './shared-websocket.service';

export interface NotificationDTO {
  id: number;
  message: string;
  type: 'RIDE_ASSIGNED' | 'RIDE_STARTED' | 'PANIC' | 'RIDE_FINISHED' | 'ADDED_AS_PASSENGER' | 'NO_DRIVERS_AVAILABLE' | 'RIDE_REQUEST_REJECTED' | 'RIDE_REQUEST_ACCEPTED' | 'SCHEDULED_RIDE_REMINDER';
  createdAt: string;
  seen: boolean;
  relatedEntityId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8081/api/notifications';

  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private isInitialized = false;
  private currentToken: string | null = null;
  private messageSubscription: any = null;

  // Observables for real-time notifications
  private notificationsSubject = new BehaviorSubject<NotificationDTO[]>([]);
  private unreadCountSubject = new BehaviorSubject<number>(0);
  private newNotificationSubject = new BehaviorSubject<NotificationDTO | null>(null);

  public notifications$ = this.notificationsSubject.asObservable();
  public unreadCount$ = this.unreadCountSubject.asObservable();
  public newNotification$ = this.newNotificationSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    private ngZone: NgZone,
    private sharedWebSocket: SharedWebSocketService
  ) {}

  // Connect to WebSocket for real-time notifications using the shared service
  connectToNotifications(token: string): void {
    this.currentToken = token;

    // If already subscribed, don't subscribe again
    if (this.messageSubscription) {
      return;
    }

    // Connect the shared WebSocket
    this.sharedWebSocket.connect();

    // Subscribe to messages from the shared WebSocket
    this.messageSubscription = this.sharedWebSocket.message$.subscribe(
      (message: any) => {
        this.handleWebSocketMessage(message);
      },
      (error: any) => {
        console.error('Error in WebSocket message stream:', error);
      }
    );
  }

  private handleWebSocketMessage(data: any): void {
    console.log('WebSocket message received, action:', data.action);
    console.log('Full message data:', JSON.stringify(data));

    switch (data.action) {
      case 'INITIAL_STATE':
        // Backend sends all notifications on connection
        const notifications = data.data as NotificationDTO[];
        console.log('Received INITIAL_STATE with', notifications.length, 'notifications');
        console.log('Notifications:', JSON.stringify(notifications));
        this.notificationsSubject.next(notifications);

        // Calculate unread count from initial notifications
        const unreadCount = notifications.filter(n => !n.seen).length;
        console.log('Calculated unread count from INITIAL_STATE:', unreadCount);
        this.unreadCountSubject.next(unreadCount);
        break;

      case 'NEW_NOTIFICATION':
        const notification = data.data as NotificationDTO;
        console.log('NEW_NOTIFICATION received:', notification);
        console.log('Notification full object:', JSON.stringify(notification));
        console.log('Before update - Current notifications:', JSON.stringify(this.notificationsSubject.value));

        this.newNotificationSubject.next(notification);
        this.updateNotificationsList(notification);
        this.incrementUnreadCount();

        console.log('After update - Current notifications:', JSON.stringify(this.notificationsSubject.value));
        console.log('After update - Current unread count:', this.unreadCountSubject.value);

        this.showBrowserNotification(notification);
        break;

      case 'echo':
        console.log('Echo received');
        break;

      default:
        console.log('Unknown action:', data.action);
        break;
    }
  }

  private updateNotificationsList(newNotification: NotificationDTO): void {
    const current = this.notificationsSubject.value;
    console.log('[updateNotificationsList] Current count:', current.length, 'Adding notification:', newNotification.id);
    const updated = [newNotification, ...current];
    console.log('[updateNotificationsList] Updated count:', updated.length);
    this.notificationsSubject.next(updated);
  }

  private incrementUnreadCount(): void {
    const current = this.unreadCountSubject.value;
    const updated = current + 1;
    console.log('[incrementUnreadCount] From', current, 'to', updated);
    this.unreadCountSubject.next(updated);
  }

  private attemptReconnect(): void {
    if (!this.currentToken) {
      return;
    }

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        if (this.currentToken) {
          this.connectToNotifications(this.currentToken);
        }
      }, this.reconnectDelay);
    }
  }

  disconnect(): void {
    this.currentToken = null;

    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
      this.messageSubscription = null;
    }

    this.sharedWebSocket.disconnect();

    this.reconnectAttempts = 0;
    this.isInitialized = false;
    this.notificationsSubject.next([]);
    this.unreadCountSubject.next(0);
    this.newNotificationSubject.next(null);
  }

  private showBrowserNotification(notification: NotificationDTO): void {
    if (!('Notification' in window)) {
      return;
    }

    if (Notification.permission === 'granted') {
      new Notification('RideNow', {
        body: notification.message,
        icon: '/assets/icons/logo.png'
      });
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          new Notification('RideNow', {
            body: notification.message,
            icon: '/assets/icons/logo.png'
          });
        }
      });
    }
  }

  getAllNotifications(): Observable<NotificationDTO[]> {
    return this.http.get<NotificationDTO[]>(this.apiUrl);
  }

  getUnseenNotifications(): Observable<NotificationDTO[]> {
    return this.http.get<NotificationDTO[]>(`${this.apiUrl}/unseen`);
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/count`);
  }

  markNotificationAsSeen(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/seen`, {});
  }

  markAllNotificationsAsSeen(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/mark-all-seen`, {});
  }

  deleteNotification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }

  initializeNotifications(): void {
    const role = localStorage.getItem('role');

    if (role !== 'USER' && role !== 'DRIVER') {
      return;
    }

    if (this.isInitialized) {
      return;
    }

    this.isInitialized = true;

    this.getAllNotifications().subscribe({
      next: (notifications) => {
        this.notificationsSubject.next(notifications);
      },
      error: (error) => {
        console.error('Failed to load notifications:', error);
        this.isInitialized = false;
      }
    });

    this.getUnreadCount().subscribe({
      next: (response) => {
        this.unreadCountSubject.next(response.count);
      },
      error: (error) => {
        console.error('Failed to load unread count:', error);
      }
    });
  }

  // Load notifications and return observable for sequencing
  loadAndConnectNotifications(token: string): Observable<boolean> {
    const role = localStorage.getItem('role');
    console.log('loadAndConnectNotifications called, role:', role);

    if (role !== 'USER' && role !== 'DRIVER') {
      console.log('Role not eligible for notifications');
      return new Observable(observer => {
        observer.next(false);
        observer.complete();
      });
    }

    // Store token for reconnection
    this.currentToken = token;

    // Return observable that completes when WebSocket is connected
    return new Observable(observer => {
      console.log('Connecting to shared WebSocket for notifications...');

      // Connect the shared WebSocket (it handles its own retry logic)
      this.sharedWebSocket.connect();

      // Wait for INITIAL_STATE message
      let initialStateReceived = false;
      const initialStateTimeout = setTimeout(() => {
        if (!initialStateReceived) {
          console.warn('[Notification] INITIAL_STATE not received within 2 seconds, completing anyway');
          initialStateReceived = true;
          observer.next(true);
          observer.complete();
        }
      }, 2000);

      // Subscribe to messages from the shared WebSocket
      const subscription = this.sharedWebSocket.message$.subscribe(
        (message: any) => {
          // Complete the observable when we receive INITIAL_STATE
          if (message.action === 'INITIAL_STATE' && !initialStateReceived) {
            console.log('[Notification] INITIAL_STATE received, completing observable');
            initialStateReceived = true;
            clearTimeout(initialStateTimeout);
            observer.next(true);
            observer.complete();
          }

          // Process the message
          this.handleWebSocketMessage(message);
        },
        (error: any) => {
          console.error('[Notification] Error in WebSocket message stream:', error);
          initialStateReceived = true;
          clearTimeout(initialStateTimeout);
          observer.error(error);
        }
      );

      // Store subscription for cleanup
      if (this.messageSubscription) {
        this.messageSubscription.unsubscribe();
      }
      this.messageSubscription = subscription;
    });
  }

  updateLocalUnreadCount(decrease: number = 1): void {
    const current = Math.max(0, this.unreadCountSubject.value - decrease);
    this.unreadCountSubject.next(current);
  }

  removeNotificationFromList(notificationId: number): void {
    const current = this.notificationsSubject.value;
    const notification = current.find(n => n.id === notificationId);

    if (notification && !notification.seen) {
      this.updateLocalUnreadCount();
    }

    const updated = current.filter(n => n.id !== notificationId);
    this.notificationsSubject.next(updated);
  }

  handleNotificationClick(notification: NotificationDTO): void {
    if (!notification.seen) {
      this.markNotificationAsSeen(notification.id).subscribe({
        next: () => {
          notification.seen = true;
          this.updateLocalUnreadCount();
        },
        error: (error) => console.error('Failed to mark notification as seen:', error)
      });
    }

    switch (notification.type) {
      case 'ADDED_AS_PASSENGER':
        this.router.navigate(['/upcoming-rides']);
        break;

      case 'RIDE_STARTED':
        this.router.navigate(['/current-ride']);
        break;

      case 'RIDE_FINISHED':
        if (notification.relatedEntityId) {
          this.router.navigate(['/rating', notification.relatedEntityId]);
        }
        break;

      case 'RIDE_ASSIGNED':
        this.router.navigate(['/upcoming-rides']);
        break;

      case 'RIDE_REQUEST_ACCEPTED':
        if (notification.relatedEntityId) {
          this.router.navigate(['/upcoming-rides']);
        }
        break;

      case 'SCHEDULED_RIDE_REMINDER':
        if (notification.relatedEntityId) {
          this.router.navigate(['/upcoming-rides']);
        }
        break;

      case 'NO_DRIVERS_AVAILABLE':
      case 'RIDE_REQUEST_REJECTED':
        // No navigation needed for rejection notifications
        break;

      default:
        break;
    }
  }
}
