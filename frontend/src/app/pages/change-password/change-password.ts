import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';

// Page komponenta za promenu lozinke korisnika
@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePasswordPage {
  // Greška pri validaciji ili promeni lozinke
  passwordError: string | null = null;

  // Flag za prikaz trenutne lozinke
  showCurrent = false;
  // Flag za prikaz nove lozinke
  showNew = false;
  // Flag za prikaz potvrde lozinke
  showConfirm = false;
  
  // Development user ID
  private readonly DEV_USER_ID = 9;

  constructor(private router: Router, private userService: UserService) {}

  goBack() {
    this.router.navigate(['/profile']);
  }

  // Validira i menja lozinku korisnika
  changePassword(current: string, next: string, confirm: string) {
    this.passwordError = null;

    if (!current) {
      this.passwordError = 'Please enter your current password.';
      return;
    }

    if (!next || next.length < 6) {
      this.passwordError = 'New password must be at least 6 characters.';
      return;
    }

    if (next !== confirm) {
      this.passwordError = 'New password and confirmation do not match.';
      return;
    }

    
    let userId = this.DEV_USER_ID;
    try {
      const raw = localStorage.getItem('user');
      if (raw) {
        const parsed = JSON.parse(raw as string);
        if (parsed && parsed.id) userId = Number(parsed.id);
      }
    } catch (e) {
      
    }

    const payload = {
      currentPassword: current,
      newPassword: next,
      confirmNewPassword: confirm,
    };

    this.userService.changePassword(payload).subscribe({
      next: () => {
        this.router.navigate(['/profile'], {
          state: {
            toastMessage: 'Your password has been changed successfully.',
          },
        });
      },
      error: (err) => {
        console.error('Change password failed', err);
        this.passwordError = err?.error?.message || 'Failed to change password.';
      },
    });
  }

  // Toggle-uje vidljivost lozinke za dato polje
  toggleShow(field: 'current' | 'new' | 'confirm') {
    if (field === 'current') this.showCurrent = !this.showCurrent;
    if (field === 'new') this.showNew = !this.showNew;
    if (field === 'confirm') this.showConfirm = !this.showConfirm;
  }
}
