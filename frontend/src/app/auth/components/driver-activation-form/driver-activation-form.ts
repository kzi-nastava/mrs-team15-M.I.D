import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { ActivatedRoute, Router } from '@angular/router'
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DriverService } from '../../../services/driver.service';

@Component({
  selector: 'app-driver-activation-form',
  imports: [Button, InputComponent, CommonModule],
  standalone: true, 
  templateUrl: './driver-activation-form.html',
  styleUrl: './driver-activation-form.css',
})

export class DriverActivationForm implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router, private driverService: DriverService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token');
    if (!this.token) {
      this.showMessageToast('Invalid activation link.');
      return;
    }
  }

  showMessage = false;
  message = '';
  token: string | null = null;
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

  newPassword : string = '';
  newConfirmedPassword : string = '';

  validator : FromValidator = new FromValidator();

  activateDriver(): void {
      console.log('DriverActivationForm.activateDriver token=', this.token, 'payload=', { password: this.newPassword.trim(), passwordConfirmation: this.newConfirmedPassword.trim(), token: this.token });
      this.driverService.driverActivate(this.token!, { password: this.newPassword.trim(), passwordConfirmation: this.newConfirmedPassword.trim(), token: this.token! }).subscribe({
      next: (res: any) => {
        this.showMessageToast(res.message);
        setTimeout(() => { this.router.navigate(['/login']);}, 4000);
      },
      error: (err: any) => {
        console.error('Driver activation error', err);
        const status = err?.status;
        const serverMsg = err?.error?.message ?? err?.error ?? err?.message;
        const msg = serverMsg ?? 'Activation failed. Please try again.';
        this.showMessageToast(`(${status}) ${msg}`);
      }
    });
  }

  hasErrors() : boolean {
    return !!(this.validator.passwordError(this.newPassword) || this.validator.confirmPasswordError(this.newPassword, this.newConfirmedPassword));
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
  }
}
