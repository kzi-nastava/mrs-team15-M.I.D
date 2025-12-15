import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { RouterLink } from '@angular/router'
@Component({
  selector: 'app-forgot-password-form',
  imports: [Button, InputComponent, RouterLink],
  templateUrl: './forgot-password-form.html',
  styleUrl: './forgot-password-form.css',
})
export class ForgotPasswordForm {

}
