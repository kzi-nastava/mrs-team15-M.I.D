import { Component, ViewChild, ElementRef, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VehicleForm } from '../vehicle-form/vehicle-form';
import { Router } from '@angular/router';
import { Button } from '../button/button';

@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, VehicleForm],
  templateUrl: './profile-form.html',
  styleUrl: './profile-form.css',
})
export class ProfileForm implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  userAvatar: string = '';
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

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

  private emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  
  private phonePattern = /^(\+381|0)[0-9]{9,10}$/;

  isEmailValid(email: string): boolean {
    if (!email) return false;
    return this.emailPattern.test(email);
  }

  isPhoneValid(phone: string): boolean {
    if (!phone) return false;
    return this.phonePattern.test(phone);
  }

  getEmailErrorMessage(): string {
    if (!this.user.email) return 'Email is required';
    if (!this.isEmailValid(this.user.email)) return 'Email is invalid';
    return '';
  }

  getPhoneErrorMessage(): string {
    if (!this.user.phone) return 'Phone number is required';
    if (!this.isPhoneValid(this.user.phone)) return 'Phone number is invalid (e.g: 0601234567 or +381601234567)';
    return '';
  }

  isFieldEmpty(field: string): boolean {
    return !field || field.trim() === '';
  }

  private licensePlatePattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;

  isLicensePlateValid(licensePlate: string): boolean {
    if (!licensePlate) return false;
    return this.licensePlatePattern.test(licensePlate.toUpperCase());
  }

  hasValidationErrors(): boolean {
    if (this.isFieldEmpty(this.user.firstName) || this.isFieldEmpty(this.user.lastName) || this.isFieldEmpty(this.user.address)) {
      return true;
    }
    if (!this.isEmailValid(this.user.email) || !this.isPhoneValid(this.user.phone)) {
      return true;
    }
    if (this.user.role === 'driver' && !this.isLicensePlateValid(this.user.vehicle.licensePlate)) {
      return true;
    }
    return false;
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
    this.toastType = 'success';
    this.showToast = true;
    this.cdr.detectChanges();

    setTimeout(() => {
      this.showToast = false;
      this.cdr.detectChanges();
    }, 3000);
  }
}
