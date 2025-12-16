import { Component, ViewChild, ElementRef } from '@angular/core';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { VehicleForm } from '../vehicle-form/vehicle-form'; // <-- dodaj ovo

@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [Button, InputComponent, VehicleForm], // <-- dodaj ovde
  templateUrl: './profile-form.html',
  styleUrl: './profile-form.css',
})
export class ProfileForm {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  user = {
    firstName: 'John',
    lastName: 'Doe',
    phone: '0601234567',
    address: 'Bulevar cara Lazara 1, Novi Sad',
    email: 'john.doe@example.com',
    role: 'driver',
    activeHours: 8,
    vehicle: {
      licensePlate: 'NS123AB',
      model: 'Golf 7',
      seats: 4,
      type: 'Standard',
      petFriendly: true,
      babyFriendly: true,
    }
  };

  // Klik na sliku otvara file picker
  onSelectPhoto(): void {
    this.fileInput.nativeElement.click();
  }

  // File je izabran, ali trenutno ništa ne radimo sa njim
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      console.log('File selected:', input.files[0].name);
      // trenutno ne radimo ništa sa slikom
    }
  }

  onUploadPhoto(): void {
    console.log('Upload photo clicked');
  }

  onSave(): void {
    console.log('Save profile', this.user);
  }
}
