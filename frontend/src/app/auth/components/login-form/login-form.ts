import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { RouterLink } from '@angular/router'
@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [Button, InputComponent, RouterLink],
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
}
