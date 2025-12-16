import { Component } from '@angular/core';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [Button, InputComponent],
  templateUrl: './profile-form.html',
  styleUrl: './profile-form.css',
})
export class ProfileForm {
  user = {
    firstName: 'John',
    lastName: 'Doe',
    phone: '0601234567',
    address: 'Bulevar cara Lazara 1, Novi Sad',
    email: 'john.doe@example.com',
  };

  onUploadPhoto(): void {
    console.log('Upload photo clicked');
  }

  onSave(): void {
    // Hook to save profile — wire to API later
    console.log('Save profile', this.user);
  }
}
