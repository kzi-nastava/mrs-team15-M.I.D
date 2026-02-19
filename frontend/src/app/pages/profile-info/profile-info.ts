import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileForm } from '../../shared/components/profile-form/profile-form';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile-info',
  standalone: true,
  imports: [ProfileForm, CommonModule],
  templateUrl: './profile-info.html',
  styleUrl: './profile-info.css',
})
export class ProfileInfo implements OnInit {
  user = {
    name: 'John Doe',
    email: 'john.doe@example.com',
    phone: '0601234567',
    address: 'Bulevar cara Lazara 1, Novi Sad',
  };

  isBlocked: boolean = false;
  blockReason: string = '';
  blockedAt: string = '';
  isDriver: boolean = false;

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Check if user is a driver
    const userRole = this.authService.getRole();
    this.isDriver = userRole === 'DRIVER';

    // Check if user is blocked (only for drivers)
    if (this.isDriver) {
      this.userService.getBlockedStatus().subscribe({
        next: (status) => {
          if (status.blocked) {
            this.isBlocked = true;
            this.blockReason = status.reason || 'No reason provided';
            this.blockedAt = status.blockedAt ? new Date(status.blockedAt).toLocaleString() : 'Unknown';
          }
        },
        error: (err) => {
          console.error('Error checking blocked status:', err);
        }
      });
    }
  }

  onEdit(): void {
    console.log('Edit profile clicked');
  }
}
