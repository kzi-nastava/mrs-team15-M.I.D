import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { RouterLink } from '@angular/router'
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-forgot-password-form',
  imports: [Button, InputComponent, RouterLink, CommonModule],
  templateUrl: './forgot-password-form.html',
  styleUrl: './forgot-password-form.css',
})
export class ForgotPasswordForm {
  email : string = '';
  validator : FromValidator = new FromValidator();

  hasErrors() : boolean{
    return !!(this.validator.emailError(this.email))
  }
}
