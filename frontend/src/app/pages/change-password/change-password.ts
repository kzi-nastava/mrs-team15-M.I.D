import { Component } from '@angular/core';
import { Location } from '@angular/common';
import { Button } from '../../shared/components/button/button';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [Button],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePasswordPage {
  passwordError: string | null = null;

  showCurrent = false;
  showNew = false;
  showConfirm = false;
  
  constructor(private location: Location) {}

  goBack() {
    this.location.back();
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

    console.log('Change password:', { current, next });
  }
  toggleShow(field: 'current' | 'new' | 'confirm') {
    if (field === 'current') this.showCurrent = !this.showCurrent;
    if (field === 'new') this.showNew = !this.showNew;
    if (field === 'confirm') this.showConfirm = !this.showConfirm;
  }
}
