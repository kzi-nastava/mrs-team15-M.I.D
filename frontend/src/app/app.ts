import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar';
import { LocationTrackingService } from './services/location-tracking.service';
import { TokenExpirationService } from './services/token-expiration.service';
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

  constructor(private locationTrackingService: LocationTrackingService, private tokenExpirationService: TokenExpirationService) {
    // Inject to initialize the service
  }

  ngOnInit(): void {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      this.tokenExpirationService.startTokenExpirationCheck();
    }
  }

  ngOnDestroy(): void {
    this.tokenExpirationService.stopTokenExpirationCheck();
  }

   isAdmin(): boolean {
    const role = localStorage.getItem('role');
    console.log('Checking if admin:', role === 'ADMIN');
    return role === 'ADMIN';
  }
}
