import { ChangeDetectorRef, Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component'
import { Router, RouterLink } from '@angular/router'
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
@Component({
  selector: 'app-forgot-password-form',
  imports: [Button, InputComponent, RouterLink, CommonModule],
  templateUrl: './forgot-password-form.html',
  styleUrl: './forgot-password-form.css',
})
export class ForgotPasswordForm {

   constructor(private cdr: ChangeDetectorRef, private authService : AuthService, private router : Router){}
   
  email : string = '';
  validator : FromValidator = new FromValidator();
  message = '';
  showMessage = false;

  hasErrors() : boolean{
    return !!(this.validator.emailError(this.email))
  }
  
  forgotPassword(){
    if(this.hasErrors()) { return ;}
    
    const data = {email: this.email};

    this.authService.forgotPassword(data).subscribe({
      next: () => {
        this.showMessageToast("Check your email for a link to reset your password, it will arrive shortly.");
      },
      error: (err) => {
        if (typeof err.error === 'string') {
          this.showMessageToast(err.error);
        } else {
          this.showMessageToast('Something went wrong. Please try again later.');
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
}
