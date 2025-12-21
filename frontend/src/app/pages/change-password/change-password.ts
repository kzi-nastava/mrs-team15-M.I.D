import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Button } from '../../shared/components/button/button';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [Button, CommonModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePasswordPage {
  passwordError: string | null = null;

  showCurrent = false;
  showNew = false;
  showConfirm = false;

  constructor(private router: Router) {}

  goBack() {
    this.router.navigate(['/profile']);
  }

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

    this.router.navigate(['/profile'], {
      state: {
        toastMessage: 'Your password has been changed successfully.',
      },
    });
  }

  toggleShow(field: 'current' | 'new' | 'confirm') {
    if (field === 'current') this.showCurrent = !this.showCurrent;
    if (field === 'new') this.showNew = !this.showNew;
    if (field === 'confirm') this.showConfirm = !this.showConfirm;
  }
}
