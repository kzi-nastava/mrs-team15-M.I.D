import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { ActivatedRoute, Router, RouterLink } from '@angular/router'
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-driver-activation-form',
  imports: [Button, InputComponent, RouterLink, CommonModule],
  standalone: true, 
  templateUrl: './driver-activation-form.html',
  styleUrl: './driver-activation-form.css',
})

export class DriverActivationForm implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router, private authService: AuthService, private cdr: ChangeDetectorRef) {}

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
    const data = {newPassword: this.newPassword, confirmNewPassword: this.newConfirmedPassword}
    this.authService.resetPassword(this.token!, data).subscribe({
      next: (res) => {
        this.showMessageToast(res.message);
        setTimeout(() => { this.router.navigate(['/login']);}, 4000);
      },
      error: (err) => {
        const msg = err.error?.message ?? 'Activation failed. Please try again.';
        this.showMessageToast(msg);
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
