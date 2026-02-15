import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificationWebSocketService, PanicAlert } from '../../../services/notification-websocket.service';
import { PanicAlertService } from '../../../services/panic-alert.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-panic-notification',
  imports: [CommonModule],
  templateUrl: './panic-notification.html',
  styleUrl: './panic-notification.css',
})

export class PanicNotification implements OnInit, OnDestroy {
  unresolvedAlerts: PanicAlert[] = [];
  connectionStatus = false;
  private alertSubscription?: Subscription;
  private connectionSubscription?: Subscription;

  constructor(
    private notificationService: NotificationWebSocketService,
    private panicAlertService: PanicAlertService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('PanicNotification initialized');
    
    // Subscribe to unresolved alerts
    this.alertSubscription = this.notificationService.unresolvedAlerts$.subscribe(alerts => {
      console.log('Unresolved alerts updated:', alerts.length);
      this.unresolvedAlerts = alerts;
      this.cdr.detectChanges();
    });

    // Subscribe to connection status
    this.connectionSubscription = this.notificationService.connectionStatus$.subscribe(status => {
      this.connectionStatus = status;
      console.log('Connection status:', status ? 'Connected' : 'Disconnected');
      this.cdr.detectChanges();
    });
  }

  viewRideDetails(rideId: number): void {
    this.router.navigate(["/panic-alerts"])
  }

  getTimeAgo(timestamp: string): string {
    try {
      const alertTime = new Date(timestamp);
      const now = new Date();
      const seconds = Math.floor((now.getTime() - alertTime.getTime()) / 1000);
      
      if (seconds < 60) return `${seconds}s ago`;
      
      const minutes = Math.floor(seconds / 60);
      if (minutes < 60) return `${minutes}m ago`;
      
      const hours = Math.floor(minutes / 60);
      if (hours < 24) return `${hours}h ago`;
      
      const days = Math.floor(hours / 24);
      return `${days}d ago`;
    } catch (error) {
      console.error('Error parsing timestamp:', error);
      return 'just now';
    }
  }

  // Dismiss alert (just hide from this admin, doesn't resolve)
  dismissAlert(alert: PanicAlert, event: Event): void {
    event.stopPropagation();
    console.log('Dismissing alert locally:', alert.id);
    this.unresolvedAlerts = this.unresolvedAlerts.filter(a => a.id !== alert.id);
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    console.log('PanicNotification destroyed');
    this.alertSubscription?.unsubscribe();
    this.connectionSubscription?.unsubscribe();
  }
}