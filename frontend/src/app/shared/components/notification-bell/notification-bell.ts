import { Component, OnInit, OnDestroy, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, NotificationDTO } from '../../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.css'
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: NotificationDTO[] = [];
  unreadCount: number = 0;
  showDropdown: boolean = false;

  private subscriptions = new Subscription();

  constructor(private notificationService: NotificationService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    // Subscribe to notifications
    this.subscriptions.add(
      this.notificationService.notifications$.subscribe(notifications => {
        this.notifications = notifications;
        this.cdr.detectChanges();
      })
    );

    // Subscribe to unread count
    this.subscriptions.add(
      this.notificationService.unreadCount$.subscribe(count => {
        this.unreadCount = count;
        this.cdr.detectChanges();
      })
    );

    // Subscribe to new notifications (for toast/sound effects if needed)
    this.subscriptions.add(
      this.notificationService.newNotification$.subscribe(notification => {
        if (notification) {
          this.cdr.detectChanges();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  closeDropdown(): void {
    this.showDropdown = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const clickedInside = target.closest('.notification-bell');
    if (!clickedInside && this.showDropdown) {
      this.closeDropdown();
    }
  }

  markAsSeen(notification: NotificationDTO): void {
    this.notificationService.handleNotificationClick(notification);
    this.closeDropdown();
  }

  markAllAsSeen(): void {
    this.notificationService.markAllNotificationsAsSeen().subscribe({
      next: () => {
        this.notifications.forEach(n => n.seen = true);
        this.unreadCount = 0;
      },
      error: (error) => console.error('Failed to mark all as seen:', error)
    });
  }

  deleteNotification(event: MouseEvent, notification: NotificationDTO): void {
    // Stop propagation to prevent marking as seen
    event.stopPropagation();

    this.notificationService.deleteNotification(notification.id).subscribe({
      next: () => {
        this.notificationService.removeNotificationFromList(notification.id);
      },
      error: (error) => {
        console.error('Failed to delete notification:', error);
      }
    });
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'ADDED_AS_PASSENGER':
        return 'bi bi-person-plus-fill';
      case 'RIDE_ASSIGNED':
        return 'bi bi-car-front-fill';
      case 'RIDE_STARTED':
        return 'bi bi-play-circle-fill';
      case 'RIDE_FINISHED':
        return 'bi bi-check-circle-fill';
      case 'PANIC':
        return 'bi bi-exclamation-triangle-fill';
      case 'RIDE_REQUEST_ACCEPTED':
        return 'bi bi-check2-circle';
      case 'NO_DRIVERS_AVAILABLE':
        return 'bi bi-x-circle-fill';
      case 'RIDE_REQUEST_REJECTED':
        return 'bi bi-x-circle-fill';
      case 'SCHEDULED_RIDE_REMINDER':
        return 'bi bi-alarm-fill';
      default:
        return 'bi bi-bell-fill';
    }
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

    return date.toLocaleDateString();
  }

  trackByNotificationId(index: number, notification: NotificationDTO): number {
    return notification.id;
  }
}
