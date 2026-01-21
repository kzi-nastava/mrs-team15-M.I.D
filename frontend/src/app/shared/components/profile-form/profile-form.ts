import { Component, ViewChild, ElementRef, ChangeDetectorRef, OnInit } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
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
  selectedProfileFile: File | null = null;
  userAvatar: string | SafeUrl = '';
  backendUrl = 'http://localhost:8081';


  // ---------------- UI STATE ----------------
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
    private userService: UserService,
    private sanitizer: DomSanitizer
  ) {}

  // Get logged-in user's id from localStorage, fallback to DEV id
  private getCurrentUserId(): number {
    let userId = 11; // DEV user id
    try {
      const raw = localStorage.getItem('user');
      if (raw) {
        const parsed = JSON.parse(raw as string);
        if (parsed && parsed.id) userId = Number(parsed.id);
      }
    } catch (e) {
      // ignore and use DEV id
    }
    return userId;
  }

  // ---------------- INIT ----------------
  ngOnInit(): void {
    this.userService.getUser().subscribe({
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
      this.userAvatar = this.backendUrl + res.profileImage;
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
      this.selectedProfileFile = file;
      const blobUrl = URL.createObjectURL(file);
      // sanitize blob URL for Angular binding
      this.userAvatar = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
      console.debug('[ProfileForm] selected file:', { name: file.name, type: file.type, size: file.size, blobUrl });
      this.cdr.detectChanges();
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
    // Build FormData so backend can accept multipart/form-data (including image file)
    const formData = new FormData();
    formData.append('email', this.user.email || '');
    formData.append('firstName', this.user.firstName || '');
    formData.append('lastName', this.user.lastName || '');
    formData.append('phoneNumber', this.user.phone || '');
    formData.append('address', this.user.address || '');

    if (this.selectedProfileFile) {
      formData.append('profileImage', this.selectedProfileFile);
    }

    if (this.user.role === 'driver') {
      // driver-specific fields
      formData.append('licensePlate', this.user.vehicle.licensePlate ? String(this.user.vehicle.licensePlate).toUpperCase().trim() : '');
      formData.append('vehicleModel', this.user.vehicle.model || '');
      formData.append('numberOfSeats', String(this.user.vehicle.seats || 0));
      formData.append('vehicleType', this.user.vehicle.type || '');
      formData.append('babyFriendly', String(!!this.user.vehicle.babyFriendly));
      formData.append('petFriendly', String(!!this.user.vehicle.petFriendly));

      this.userService.requestDriverChange(formData).subscribe({
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

    // non-driver update
    this.userService.updateUser( formData).subscribe({
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
