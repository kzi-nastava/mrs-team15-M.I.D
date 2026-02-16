import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar';
import { LocationTrackingService } from './services/location-tracking.service';
import { TokenExpirationService } from './services/token-expiration.service';
import { NotificationService } from './services/notification.service';
import { PanicNotification } from './ride/components/panic-notification/panic-notification';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, PanicNotification, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Ride Now');

  constructor(
    private locationTrackingService: LocationTrackingService,
    private tokenExpirationService: TokenExpirationService,
    private notificationService: NotificationService
  ) {
    // Inject to initialize the service
  }

  ngOnInit(): void {
    const token = localStorage.getItem('jwtToken');
    const role = localStorage.getItem('role');

    console.debug('[App] ngOnInit - token:', token ? 'present' : 'missing', 'role:', role);

    if (token) {
      this.tokenExpirationService.startTokenExpirationCheck();

      // Initialize notifications for users and drivers
      if (role === 'USER' || role === 'DRIVER') {
        console.debug('[App] Initializing notifications for role:', role);
        // First load existing notifications
        this.notificationService.initializeNotifications();
        // Then connect to WebSocket for real-time updates
        this.notificationService.connectToNotifications(token);
      }
    }
  }

  ngOnDestroy(): void {
    this.tokenExpirationService.stopTokenExpirationCheck();
    this.notificationService.disconnect();
  }

   isAdmin(): boolean {
    const role = localStorage.getItem('role');
    return role === 'ADMIN';
  }
}
