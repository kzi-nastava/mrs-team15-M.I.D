import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'

@Component({
  selector: 'app-forgot-password-form',
  imports: [Button, InputComponent],
  templateUrl: './forgot-password-form.html',
  styleUrl: './forgot-password-form.css',
})
export class ForgotPasswordForm {

}
