import { Component } from '@angular/core';
import { Location } from '@angular/common';
import { Button } from '../../shared/components/button/button';
import { Router } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [Button],
  templateUrl: './unauthorized.html',
  styleUrl: './unauthorized.css',
})
export class Unauthorized {
  constructor(private location: Location, private router: Router) {}

  // Simple go back function that checks user role and redirects accordingly
  goBack(): void {
    let role = localStorage.getItem('role');
    if (role === 'ADMIN') {
      this.router.navigate(['/admin-active-rides']);
    } else if (role === 'DRIVER') {
      this.router.navigate(['/upcoming-rides']);
    } else if (role === 'USER') {
      this.router.navigate(['/upcoming-rides']);
    } else {
      this.router.navigate(['/']);
    }
  }
}
