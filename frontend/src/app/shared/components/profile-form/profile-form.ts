import { Component, ViewChild, ElementRef, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleForm } from '../vehicle-form/vehicle-form';
import { Router } from '@angular/router';
import { Button } from '../button/button';

@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [CommonModule, FormsModule, VehicleForm],
  templateUrl: './profile-form.html',
  styleUrl: './profile-form.css',
})
export class ProfileForm implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  userAvatar: string = '';
  showToast = false;
  toastMessage = '';

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
    },
  };

  constructor(private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    const toastMessage = navigation?.extras?.state?.['toastMessage'] || history.state?.['toastMessage'];
    if (toastMessage) this.showToastMessage(toastMessage);
  }

  onSelectPhoto(): void {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.userAvatar = URL.createObjectURL(file);
    }
  }

  goToChangePassword(): void {
    this.router.navigate(['/change-password']);
  }

  onSave(): void {
    const message =
      this.user.role === 'driver'
        ? 'Your change request has been submitted and is pending admin approval.'
        : 'Changes you have made have been saved successfully.';
    this.showToastMessage(message);
  }

  private showToastMessage(message: string): void {
    this.toastMessage = message;
    this.showToast = true;
    this.cdr.detectChanges();

    setTimeout(() => {
      this.showToast = false;
      this.cdr.detectChanges();
    }, 3000);
  }
}
