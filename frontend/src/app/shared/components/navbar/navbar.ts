import { Component, signal } from '@angular/core';
import { RouterLinkWithHref } from '@angular/router';
import { Router } from '@angular/router';
import { Button } from '../button/button';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLinkWithHref, Button, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {

  constructor(private router : Router, private authService : AuthService) {}

  protected menuOpen = signal(false);

  protected toggleMenu(): void {
    this.menuOpen.set(!this.menuOpen());
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  get showLogoutButton(): boolean {
    const role = localStorage.getItem('role');
    const jwtToken = localStorage.getItem('jwtToken');
    return !!role && !!jwtToken;
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        localStorage.removeItem('role');
        localStorage.removeItem('jwtToken');
        this.router.navigate(['/login']);
      },
      error: err => {
        console.error('Logout failed', err);
      }
    });
  }
}
