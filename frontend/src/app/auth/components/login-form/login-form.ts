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

  constructor(private cdr: ChangeDetectorRef, private authService : AuthService, private router : Router){}

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
        this.showMessageToast("Login successful. Good to see you again. Where to next?");
        setTimeout(() => { this.router.navigate(['/home']); }, 4000);
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
