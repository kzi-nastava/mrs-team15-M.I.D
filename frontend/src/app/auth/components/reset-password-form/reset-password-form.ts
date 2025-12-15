import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'

@Component({
  selector: 'app-reset-password-form',
  imports: [Button, InputComponent],
  standalone: true, 
  templateUrl: './reset-password-form.html',
  styleUrl: './reset-password-form.css',
})
export class ResetPasswordForm {
  newPasswordVisible = false;
  confirmPasswordVisible = false;
  togglePassword(type: string) {
    const inputs= document.querySelectorAll<HTMLInputElement>('.password-input-wrapper input');
    if (type === 'new'){
      this.newPasswordVisible = !this.newPasswordVisible;
      if(inputs[0]) {
        inputs[0].type = this.newPasswordVisible ? 'text' : 'password';
      }
    } else if(type === 'confirm'){
      this.confirmPasswordVisible = !this.confirmPasswordVisible;
      if(inputs[1]) {
        inputs[1].type = this.confirmPasswordVisible ? 'text' : 'password';
      }
    }
  }
}
