import { ChangeDetectorRef, Component, signal } from '@angular/core';
import { RouterLinkWithHref } from '@angular/router';
import { Router } from '@angular/router';
import { Button } from '../button/button';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { DriverService } from '../../../services/driver.service';
import { DriverStatusStore } from '../../states/driver-status.store';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLinkWithHref, Button, CommonModule, FormsModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {

  constructor(private driverState: DriverStatusStore, private router : Router, private authService : AuthService, private cdr: ChangeDetectorRef, private driverService : DriverService) {}

  isUpdatingStatus = false;
  isActive = false;
  driverStatus = '';

  ngOnInit() {
    if (this.showActivityToggle) {
      this.driverService.getMyStatus().subscribe();
    }

    this.driverState.status$.subscribe(status => {
      if (!status) return;
      this.driverStatus = status;
      this.isActive = status === 'ACTIVE';
      this.cdr.detectChanges();
    });
  }

  protected menuOpen = signal(false);

  protected toggleMenu(): void {
    this.menuOpen.set(!this.menuOpen());
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  get isLoggedIn(): boolean {
    const role = localStorage.getItem('role');
    const jwtToken = localStorage.getItem('jwtToken');
    return !!role && !!jwtToken;
  }

  get showActivityToggle(): boolean {
    const role = localStorage.getItem('role');
    return role === "DRIVER";
  }

  get role(): string | null {
    const role = localStorage.getItem('role');
    return role;
  }

  message = '';
  showMessage = false;


onToggleChange(event: MouseEvent) {
  event.preventDefault();
  if (this.isUpdatingStatus) return;
  const previousStatus = this.isActive;
  const newStatus = !previousStatus;
  this.isActive = newStatus;
  const statusString = newStatus ? 'ACTIVE' : 'INACTIVE';

  this.isUpdatingStatus = true;

  this.driverService.changeDriverStatus({ status: statusString }).subscribe({
    next: (res) => {
      this.isUpdatingStatus = false;
      this.driverStatus = res.status;
      this.isActive = res.status === 'ACTIVE';

      if (res.pendingStatus) {
        this.showMessageToast('Status will be changed after the ride is complete.');
      }

      this.cdr.detectChanges();
    },
    error: (err) => {
      this.isUpdatingStatus = false;
      this.isActive = previousStatus;
      this.showMessageToast(
        typeof err.error === 'string' ? err.error : 'Status change failed. Please try again.'
      );
      this.cdr.detectChanges();
    }
  });
}

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        localStorage.removeItem('role');
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('tokenExpiration');
        this.driverState.resetStatus();
        this.isActive = false;
        this.driverStatus = '';
        this.showMessageToast( 'You have been logged out successfully. See you next time!');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        if (typeof err.error === 'string') {
          this.showMessageToast(err.error);
        } else {
          this.showMessageToast('Unable to log out right now. Please try again.');
        }
      }
    });
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false;}, 3000);
    this.cdr.detectChanges();
  }
}
