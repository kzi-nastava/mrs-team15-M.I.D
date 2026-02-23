import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';

// Page component for changing user password
// Validates current password and new password match before submitting
@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePasswordPage {
  // Error message displayed to user
  passwordError: string | null = null;

  // Toggle password visibility flags
  showCurrent = false;
  showNew = false;
  showConfirm = false;
  
  // Default user ID for development
  private readonly DEV_USER_ID = 9;

  constructor(private router: Router, private userService: UserService) {}

  // Navigates back to profile page
  goBack() {
    this.router.navigate(['/profile']);
  }

  // Validates and submits password change request
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

    // Get user ID from localStorage
    let userId = this.DEV_USER_ID;
    try {
      const raw = localStorage.getItem('user');
      if (raw) {
        const parsed = JSON.parse(raw as string);
        if (parsed && parsed.id) userId = Number(parsed.id);
      }
    } catch (e) {
      // Use dev default if parsing fails
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

  // Toggles password visibility for specified field
  toggleShow(field: 'current' | 'new' | 'confirm') {
    if (field === 'current') this.showCurrent = !this.showCurrent;
    if (field === 'new') this.showNew = !this.showNew;
    if (field === 'confirm') this.showConfirm = !this.showConfirm;
  }
}
