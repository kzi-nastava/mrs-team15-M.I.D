import { Component } from '@angular/core';
import { ProfileForm } from '../../auth/components/profile-form/profile-form';

@Component({
  selector: 'app-profile-info',
  standalone: true,
  imports: [ProfileForm],
  templateUrl: './profile-info.html',
  styleUrl: './profile-info.css',
})
export class ProfileInfo {
  user = {
    name: 'John Doe',
    email: 'john.doe@example.com',
    phone: '0601234567',
    address: 'Bulevar cara Lazara 1, Novi Sad',
  };

  onEdit(): void {
    console.log('Edit profile clicked');
  }
}
