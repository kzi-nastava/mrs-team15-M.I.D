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
    private notificationService: NotificationService
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

        console.log('Login successful - Role:', response.role);

        if (response.role === 'ADMIN') {
          setTimeout(() => { this.notificationWebSocketService.connect(); }, 500);
        } else if (response.role === 'USER' || response.role === 'DRIVER') {
          // Initialize notifications for users and drivers
          console.log('Initializing notifications after login');
          // First load existing notifications
          this.notificationService.initializeNotifications();
          // Then connect to WebSocket for real-time updates
          this.notificationService.connectToNotifications(response.token);
          setTimeout(() => { this.notificationWebSocketService.connect(); }, 500);
        }

        switch(response.role) {
          case 'ADMIN':
            setTimeout(() => { this.router.navigate(['/admin-history-overview']); }, 1000);
            return;
          case 'DRIVER':
            if(response.hasCurrentRide){
              setTimeout(() => { this.router.navigate(['/current-ride']); }, 1000);
              return;
            }
            setTimeout(() => { this.router.navigate(['/upcoming-rides']); }, 1000);
            return;
            case 'USER':
            if(response.hasCurrentRide){
              setTimeout(() => { this.router.navigate(['/current-ride']); }, 1000);
              return;
            }
            setTimeout(() => { this.router.navigate(['/ride-ordering']); }, 1000);
            return;
        }
        setTimeout(() => { this.router.navigate(['/home']); }, 1000);
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
