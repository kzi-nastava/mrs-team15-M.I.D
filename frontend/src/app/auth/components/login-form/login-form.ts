import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { RouterLink } from '@angular/router'
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
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
  validator : FromValidator = new FromValidator()

  hasErrors() : boolean{
    return this.validator.isEmailValid(this.email) === false || this.validator.isPasswordValid(this.password) === false;
  }
}
