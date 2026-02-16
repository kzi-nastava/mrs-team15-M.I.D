import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

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
  private wsUrl = 'ws://localhost:8081/api/notifications/websocket';

  private socket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private isInitialized = false;
  private currentToken: string | null = null;

  // Observables for real-time notifications
  private notificationsSubject = new BehaviorSubject<NotificationDTO[]>([]);
  private unreadCountSubject = new BehaviorSubject<number>(0);
  private newNotificationSubject = new BehaviorSubject<NotificationDTO | null>(null);

  public notifications$ = this.notificationsSubject.asObservable();
  public unreadCount$ = this.unreadCountSubject.asObservable();
  public newNotification$ = this.newNotificationSubject.asObservable();

  constructor(private http: HttpClient, private router: Router, private ngZone: NgZone) {}

  // Connect to WebSocket for real-time notifications
  connectToNotifications(token: string): void {
    this.currentToken = token;

    if (this.socket?.readyState === WebSocket.OPEN) {
      return;
    }

    if (this.socket?.readyState === WebSocket.CONNECTING) {
      return;
    }

    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }

    try {
      this.socket = new WebSocket(`${this.wsUrl}?token=${encodeURIComponent(token)}`);

      this.socket.onopen = () => {
        this.reconnectAttempts = 0;
      };

      this.socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.handleWebSocketMessage(data);
        } catch (error) {
          console.error('Error parsing notification message:', error);
        }
      };

      this.socket.onclose = (event) => {
        this.socket = null;
        if (this.currentToken && event.code !== 1000) {
          this.attemptReconnect();
        }
      };

      this.socket.onerror = (error) => {
        console.error('WebSocket connection error:', error);
      };

    } catch (error) {
      console.error('Failed to create WebSocket:', error);
      this.socket = null;
      this.attemptReconnect();
    }
  }

  private handleWebSocketMessage(data: any): void {
    this.ngZone.run(() => {
      switch (data.action) {
        case 'NEW_NOTIFICATION':
          const notification = data.data as NotificationDTO;
          this.newNotificationSubject.next(notification);
          this.updateNotificationsList(notification);
          this.incrementUnreadCount();
          this.showBrowserNotification(notification);
          break;
        case 'echo':
          break;
        default:
          break;
      }
    });
  }

  private updateNotificationsList(newNotification: NotificationDTO): void {
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([newNotification, ...current]);
  }

  private incrementUnreadCount(): void {
    this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
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

    if (this.socket) {
      this.socket.close(1000, 'User logout');
      this.socket = null;
    }

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
