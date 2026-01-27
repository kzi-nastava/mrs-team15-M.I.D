import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar';
import { LocationTrackingService } from './services/location-tracking.service';
import { TokenExpirationService } from './services/token-expiration.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent],
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
}
