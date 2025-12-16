import { Component } from '@angular/core';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-registration-form',
  imports: [Button, RouterLink, InputComponent],
  templateUrl: './registration-form.html',
  styleUrl: './registration-form.css',
})

export class RegistrationForm {
  passwordVisible = false;
  confirmPasswordVisible = false;
  togglePassword(type: string) {
    const inputs= document.querySelectorAll<HTMLInputElement>('.password-input-wrapper input');
    if (type === 'new'){
      this.passwordVisible = !this.passwordVisible;
      if(inputs[0]) {
        inputs[0].type = this.passwordVisible ? 'text' : 'password';
      }
    } else if(type === 'confirm'){
      this.confirmPasswordVisible = !this.confirmPasswordVisible;
      if(inputs[1]) {
        inputs[1].type = this.confirmPasswordVisible ? 'text' : 'password';
      }
    }
  }
}

