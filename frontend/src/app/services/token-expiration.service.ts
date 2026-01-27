import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TokenExpirationService {
  private checkInterval: any;

  constructor(private router: Router, private authService: AuthService) {}

  startTokenExpirationCheck(): void {
    this.checkInterval = setInterval(() => {
      this.checkTokenExpiration();
    }, 30000);
  }

  checkTokenExpiration(): void {
    const expirationTime = localStorage.getItem('tokenExpiration');
    
    if (!expirationTime) {
      return;
    }

    const expiresAt = parseInt(expirationTime, 10);
    const now = Date.now();

    if (now >= expiresAt) {
      this.handleExpiredToken();
    }
  }

  private handleExpiredToken(): void {
    this.stopTokenExpirationCheck();
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('role');
    localStorage.removeItem('tokenExpiration');
    
    alert('Your session has expired. Please log in again.');
    this.router.navigate(['/login']);
  }

  stopTokenExpirationCheck(): void {
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
    }
  }
}