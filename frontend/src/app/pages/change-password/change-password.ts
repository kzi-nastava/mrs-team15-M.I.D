import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePasswordPage {
  passwordError: string | null = null;

  showCurrent = false;
  showNew = false;
  showConfirm = false;
  // DEV fallback id
  private readonly DEV_USER_ID = 9;

  constructor(private router: Router, private userService: UserService) {}

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

    // determine user id (try localStorage, fallback to DEV id)
    let userId = this.DEV_USER_ID;
    try {
      const raw = localStorage.getItem('user');
      if (raw) {
        const parsed = JSON.parse(raw as string);
        if (parsed && parsed.id) userId = Number(parsed.id);
      }
    } catch (e) {
      // ignore and use DEV id
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

  toggleShow(field: 'current' | 'new' | 'confirm') {
    if (field === 'current') this.showCurrent = !this.showCurrent;
    if (field === 'new') this.showNew = !this.showNew;
    if (field === 'confirm') this.showConfirm = !this.showConfirm;
  }
}
