
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-welcome-form',
  standalone: true,
  imports: [Button, CommonModule],
  templateUrl: './welcome-form.html',
  styleUrls: ['./welcome-form.css'],
})
export class WelcomeForm {
  protected isLoggedIn: boolean = false;

  constructor() {
    this.isLoggedIn = !!(
      localStorage.getItem('user') ||
      localStorage.getItem('token') ||
      sessionStorage.getItem('user') ||
      sessionStorage.getItem('token')
    );
  }

  protected get targetRoute(): string {
    return this.isLoggedIn ? '/ride-ordering' : '/ride-estimation';
  }

}
