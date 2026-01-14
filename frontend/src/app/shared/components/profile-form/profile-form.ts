import { Component, ViewChild, ElementRef, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VehicleForm } from '../vehicle-form/vehicle-form';
import { Router } from '@angular/router';
import { Button } from '../button/button';
import { UserService } from '../../../services/user.service';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';


@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, VehicleForm],
  templateUrl: './profile-form.html',
  styleUrl: './profile-form.css'
})
export class ProfileForm implements OnInit {

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  // ---------------- DEV ----------------
  private readonly DEV_USER_ID = 5;

  // ---------------- UI STATE ----------------
  userAvatar: string = '';
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  // ---------------- MODEL ----------------
  user = {
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    email: '',
    role: 'registered_user',
    activeHours: 0,
    vehicle: {
      licensePlate: '',
      model: '',
      seats: 0,
      type: '',
      petFriendly: false,
      babyFriendly: false
    }
  };

  constructor(
    private router: Router,
    private cdr: ChangeDetectorRef,
    private userService: UserService
  ) {}

  // ---------------- INIT ----------------
  ngOnInit(): void {
    this.userService.getUser(this.DEV_USER_ID).subscribe({
      next: (res) => this.mapUser(res),
      error: (err) => {
        console.error('Failed to load user profile', err);
        this.showToastMessage('Failed to load profile', 'error');
      }
    });
  }

  // ---------------- MAPPING ----------------
  private mapUser(res: any): void {
    this.user.firstName = res.firstName;
    this.user.lastName = res.lastName;
    this.user.email = res.email;
    this.user.phone = res.phoneNumber;
    this.user.address = res.address;
    this.user.role = res.role?.toLowerCase() || this.user.role;

    if (res.profileImage) {
      this.userAvatar = res.profileImage;
    }

    if (res.licensePlate || res.vehicleModel) {
      this.user.vehicle = {
        licensePlate: res.licensePlate || '',
        model: res.vehicleModel || '',
        seats: res.numberOfSeats || 0,
        type: res.vehicleType || '',
        petFriendly: res.petFriendly || false,
        babyFriendly: res.babyFriendly || false
      };
    }


    this.cdr.detectChanges();
  }

  // ---------------- VALIDATION ----------------
  private emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  private phonePattern = /^(\+381|0)[0-9]{9,10}$/;
  private licensePlatePattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;

  isFieldEmpty(value: string): boolean {
    return !value || value.trim() === '';
  }

  isEmailValid(email: string): boolean {
    return this.emailPattern.test(email);
  }

  isPhoneValid(phone: string): boolean {
    return this.phonePattern.test(phone);
  }

  isLicensePlateValid(plate: string): boolean {
    return this.licensePlatePattern.test(plate.toUpperCase());
  }

  hasValidationErrors(): boolean {
    if (
      this.isFieldEmpty(this.user.firstName) ||
      this.isFieldEmpty(this.user.lastName) ||
      this.isFieldEmpty(this.user.address)
    ) {
      return true;
    }

    if (!this.isEmailValid(this.user.email) || !this.isPhoneValid(this.user.phone)) {
      return true;
    }

    if (
      this.user.role === 'driver' &&
      !this.isLicensePlateValid(this.user.vehicle.licensePlate)
    ) {
      return true;
    }

    return false;
  }
  getEmailErrorMessage(): string {
    if (!this.user.email) {
      return 'Email is required';
    }
    if (!this.isEmailValid(this.user.email)) {
      return 'Email is invalid';
    }
    return '';
  }

  getPhoneErrorMessage(): string {
    if (!this.user.phone) {
      return 'Phone number is required';
    }
    if (!this.isPhoneValid(this.user.phone)) {
      return 'Phone number is invalid (e.g. 0601234567 or +381601234567)';
    }
    return '';
  }


  // ---------------- IMAGE ----------------
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

  // ---------------- NAV ----------------
  goToChangePassword(): void {
    this.router.navigate(['/change-password']);
  }

  // ---------------- SAVE ----------------
  onSave(): void {
    if (this.hasValidationErrors()) {
      this.showToastMessage('Please fix validation errors.', 'error');
      return;
    }

    if (this.user.role === 'driver') {
      const driverPayload = {
        licensePlate: this.user.vehicle.licensePlate ? String(this.user.vehicle.licensePlate).toUpperCase().trim() : null,
        email: this.user.email,
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        phoneNumber: this.user.phone,
        address: this.user.address,
        profileImage: this.userAvatar || null,
        vehicleModel: this.user.vehicle.model || null,
        numberOfSeats: this.user.vehicle.seats || 0,
        vehicleType: this.user.vehicle.type || null,
        babyFriendly: this.user.vehicle.babyFriendly || false,
        petFriendly: this.user.vehicle.petFriendly || false
      };

      this.userService.requestDriverChange(this.DEV_USER_ID, driverPayload).subscribe({
        next: () => {
          this.showToastMessage('Change request sent for admin approval.');
        },
        error: (err) => {
          console.error('Driver change request failed', err);
          this.showToastMessage('Profile update failed', 'error');
        }
      });
      return;
    }

    const payload = {
      email: this.user.email,
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      phoneNumber: this.user.phone,
      address: this.user.address,
      profileImage: this.userAvatar || null
    };

    this.userService.updateUser(this.DEV_USER_ID, payload).subscribe({
      next: () => {
        this.showToastMessage('Profile updated successfully.');
      },
      error: (err) => {
        console.error('Profile update failed', err);
        this.showToastMessage('Profile update failed', 'error');
      }
    });
  }

  // ---------------- TOAST ----------------
  private showToastMessage(
    message: string,
    type: 'success' | 'error' = 'success'
  ): void {
    this.toastMessage = message;
    this.toastType = type;
    this.showToast = true;
    this.cdr.detectChanges();

    setTimeout(() => {
      this.showToast = false;
      this.cdr.detectChanges();
    }, 3000);
  }
}
