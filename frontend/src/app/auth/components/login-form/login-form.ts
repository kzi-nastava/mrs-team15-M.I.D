import { Component, ChangeDetectorRef } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { RouterLink } from '@angular/router'
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { TokenExpirationService } from '../../../services/token-expiration.service';
import { NotificationWebSocketService } from '../../../services/notification-websocket.service';
import { NotificationService } from '../../../services/notification.service';
import { DriverStatusStore } from '../../../shared/states/driver-status.store';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [Button, InputComponent, RouterLink, CommonModule],
  templateUrl: './login-form.html',
  styleUrl: './login-form.css',
})
export class LoginForm {

  constructor(
    private cdr: ChangeDetectorRef,
    private authService : AuthService,
    private router : Router,
    private tokenExpirationService: TokenExpirationService,
    private notificationWebSocketService : NotificationWebSocketService,
    private notificationService: NotificationService,
    private driverStatusStore: DriverStatusStore
  ){}

  passwordVisible = false;

  togglePassword() {
    this.passwordVisible = !this.passwordVisible;
    const input = document.querySelector<HTMLInputElement>('.password-input-wrapper input');

    if(input) {
      input.type = this.passwordVisible ? 'text' : 'password';
    }
  }

  email: string = '';
  password: string = '';
  validator : FromValidator = new FromValidator();
  message = '';
  showMessage = false;


  login(){
    if(this.hasErrors()) { return ;}

    const data = {email: this.email, password: this.password};

    this.authService.login(data).subscribe({
      next: (response) => {
        localStorage.setItem('jwtToken', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('tokenExpiration', response.expiresAt.toString());
        this.tokenExpirationService.startTokenExpirationCheck();
        this.showMessageToast("Login successful. Good to see you again. Where to next?");

        if (response.role === 'DRIVER') {
          const initialStatus = response.active ? 'ACTIVE' : 'INACTIVE';
          this.driverStatusStore.setStatus(initialStatus);
        }

        if (response.role === 'ADMIN') {
          setTimeout(() => { this.notificationWebSocketService.connect(); }, 500);
          this.navigateAfterLogin(response.role, response.hasCurrentRide);
        } else if (response.role === 'USER' || response.role === 'DRIVER') {
          // Initialize notifications for users and drivers
          console.log('[LoginForm] Initializing notifications after login');
          // Load notifications and connect to WebSocket in proper sequence
          // Wait for notifications to load before navigating
          this.notificationService.loadAndConnectNotifications(response.token).subscribe({
            next: () => {
              console.log('[LoginForm] Notifications loaded, proceeding with navigation');
              this.navigateAfterLogin(response.role, response.hasCurrentRide);
            },
            error: (error) => {
              console.error('[LoginForm] Failed to load notifications:', error);
              // Still navigate even if notifications fail
              this.navigateAfterLogin(response.role, response.hasCurrentRide);
            }
          });
          setTimeout(() => { this.notificationWebSocketService.connect(); }, 500);
        } else {
          this.navigateAfterLogin(response.role, response.hasCurrentRide);
        }
      },
      error: (err) => {
        if (typeof err.error === 'string') {
          this.showMessageToast(err.error);
        } else {
          this.showMessageToast('Login failed. Please try again.');
        }
      }
    });
  }

  private navigateAfterLogin(role: string, hasCurrentRide: boolean): void {
    switch(role) {
      case 'ADMIN':
        setTimeout(() => { this.router.navigate(['/admin-history-overview']); }, 1000);
        return;
      case 'DRIVER':
        if(hasCurrentRide){
          setTimeout(() => { this.router.navigate(['/current-ride']); }, 1000);
          return;
        }
        setTimeout(() => { this.router.navigate(['/upcoming-rides']); }, 1000);
        return;
      case 'USER':
        if(hasCurrentRide){
          setTimeout(() => { this.router.navigate(['/current-ride']); }, 1000);
          return;
        }
        setTimeout(() => { this.router.navigate(['/ride-ordering']); }, 1000);
        return;
      default:
        setTimeout(() => { this.router.navigate(['/home']); }, 1000);
    }
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false;}, 3000);
  }

  hasErrors() : boolean{
    return !!(this.validator.emailError(this.email) || this.validator.passwordError(this.password));
  }
}
