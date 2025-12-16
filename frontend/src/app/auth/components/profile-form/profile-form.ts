import { Component } from '@angular/core';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { VehicleForm } from '../vehicle-form/vehicle-form';


@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [Button, InputComponent, VehicleForm],
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
    role: 'driver',
    vehicle: {
      licensePlate: 'NS123AB',
      model: 'Golf 7',
      seats: 4,
      type: 'Standard',
      petFriendly: true,
      babyFriendly: true,
    },
    activeHours: 4
  };

  onUploadPhoto(): void {
    console.log('Upload photo clicked');
  }

  onSave(): void {
    console.log('Save profile', this.user);
  }
}
