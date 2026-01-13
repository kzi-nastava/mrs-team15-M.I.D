import { Component, ChangeDetectorRef } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { RouterLink } from '@angular/router'
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [Button, InputComponent, RouterLink, CommonModule],
  templateUrl: './login-form.html',
  styleUrl: './login-form.css',
})
export class LoginForm {
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

  constructor(private authService: AuthService, private router: Router, private cdr: ChangeDetectorRef) {}

  private showToast(message: string) {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false; }, 3000);
  }

  onSubmit(): void {
    if (this.hasErrors()) return;
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        try { localStorage.setItem('user', JSON.stringify(res)); } catch (e) {}
        this.showToast('Login successful');
        setTimeout(() => this.router.navigate(['/profile']), 500);
      },
      error: (err) => {
        console.error('Login failed', err);
        this.showToast('Login failed');
      }
    });
  }

  hasErrors() : boolean{
    return !!(this.validator.emailError(this.email) || this.validator.passwordError(this.password));
  }
}
