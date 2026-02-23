import { Component, ViewChild, ElementRef, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Button } from '../button/button';
import { AdminService } from '../../../services/admin.service';

// Form component for admin to register new drivers
// Includes personal info, vehicle details and profile image upload
@Component({
  selector: 'app-driver-register-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, Button],
  templateUrl: './driver-register-form.html',
  styleUrl: './driver-register-form.css',
})
export class DriverRegisterForm implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  // URL for displaying user avatar
  userAvatar: string = '';
  // Selected profile image file
  selectedFile: File | null = null;
  // Toast visibility
  showToast = false;
  // Toast message text
  toastMessage = '';
  // Toast type (success or error)
  toastType: 'success' | 'error' = 'success';

  // Driver personal information
  user = {
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    email: '',
    role: 'driver',
    activeHours: 0,
  };

  // Driver vehicle information
  vehicle: any = {
    licensePlate: '',
    model: '',
    seats: undefined,
    type: 'Standard',
    petFriendly: false,
    babyFriendly: false,
  };

  constructor(private cdr: ChangeDetectorRef, private adminService: AdminService) {}

  ngOnInit(): void {
    // Check if user is admin
  const role ='ADMIN';
  if (role !== 'ADMIN') {
    this.showToastMessage('Access denied', 'error');
    return;
  }
}

  // Regex patterns for validation
  private emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  private phonePattern = /^(\+381|0)[0-9]{9,10}$/;
  private licensePlatePattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;

  // Validates license plate format (e.g., NS123AB)
  isLicensePlateValid(licensePlate: string): boolean {
    if (!licensePlate) return false;
    return this.licensePlatePattern.test(licensePlate.toUpperCase());
  }

  // Checks if vehicle field is empty
  isVehicleFieldEmpty(field: string): boolean {
    return !field || field.trim() === '';
  }

  // Returns error message for license plate validation
  getLicensePlateErrorMessage(): string {
    if (this.isVehicleFieldEmpty(this.vehicle.licensePlate)) return 'License plate is required';
    if (!this.isLicensePlateValid(this.vehicle.licensePlate)) return 'License plate format is invalid (e.g: NS123AB)';
    return '';
  }

  // Returns error message for vehicle model validation
  getModelErrorMessage(): string {
    return this.isVehicleFieldEmpty(this.vehicle.model) ? 'Car model is required' : '';
  }

  // Returns error message for seats validation
  getSeatsErrorMessage(): string {
    const seatsNum = Number(this.vehicle.seats);
    if (this.vehicle.seats === undefined || this.vehicle.seats === null || this.vehicle.seats === '') {
      return 'Seats is required';
    }
    if (isNaN(seatsNum) || seatsNum < 1) {
      return 'Seats must be at least 1';
    }
    return '';
  }

  // Returns error message for vehicle type validation
  getTypeErrorMessage(): string {
    return this.isVehicleFieldEmpty(this.vehicle.type) ? 'Type is required' : '';
  }

  // Validates email format
  isEmailValid(email: string): boolean {
    if (!email) return false;
    return this.emailPattern.test(email);
  }

  // Validates phone number format
  isPhoneValid(phone: string): boolean {
    if (!phone) return false;
    return this.phonePattern.test(phone);
  }

  // Returns error message for email validation
  getEmailErrorMessage(): string {
    if (!this.user.email) return 'Email is required';
    if (!this.isEmailValid(this.user.email)) return 'Email is invalid';
    return '';
  }

  // Returns error message for phone validation
  getPhoneErrorMessage(): string {
    if (!this.user.phone) return 'Phone number is required';
    if (!this.isPhoneValid(this.user.phone)) return 'Phone number is invalid (e.g: 0601234567 or +381601234567)';
    return '';
  }

  // Checks if personal field is empty
  isFieldEmpty(field: string): boolean {
    return !field || field.trim() === '';
  }

  // Triggers file input click for photo selection
  onSelectPhoto(): void {
    this.fileInput.nativeElement.click();
  }

  // Handles file selection from input
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.selectedFile = file;
      this.userAvatar = URL.createObjectURL(file);
    }
  }

  // Displays toast message for 3 seconds
  private showToastMessage(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.showToast = true;
    this.cdr.detectChanges();

    setTimeout(() => {
      this.showToast = false;
      this.cdr.detectChanges();
    }, 3000);
  }

  private mapVehicleType(type: string): string {
    if (!type) return 'STANDARD';
    const t = type.toLowerCase();
    if (t.includes('lux') || t === 'luksuz' || t === 'luxury') return 'LUXURY';
    if (t.includes('van') || t === 'kombi') return 'VAN';
    return 'STANDARD';
  }

  onSubmit(): void {
    console.log('DriverRegisterForm.onSubmit called', { user: this.user, vehicle: this.vehicle });
    // basic validation
    if (this.isFieldEmpty(this.user.firstName) || this.isFieldEmpty(this.user.lastName) || !this.isEmailValid(this.user.email) || !this.isPhoneValid(this.user.phone)) {
      this.showToastMessage('Please fill in required personal fields correctly', 'error');
      return;
    }
    if (this.isVehicleFieldEmpty(this.vehicle.licensePlate) || !this.isLicensePlateValid(this.vehicle.licensePlate) || this.isVehicleFieldEmpty(this.vehicle.model) || !this.vehicle.seats) {
      this.showToastMessage('Please fill in required vehicle fields correctly', 'error');
      return;
    }

    let adminId = 1;
    try {
      const userJson = localStorage.getItem('user');
      if (userJson) {
        const parsed = JSON.parse(userJson);
        if (parsed && parsed.id && parsed.role && parsed.role.toLowerCase() === 'admin') {
          adminId = parsed.id;
        }
      }
    } catch (e) {
      // ignore, fallback to 1
    }

    // Create FormData for multipart/form-data request
    const formData = new FormData();
    formData.append('email', this.user.email);
    formData.append('firstName', this.user.firstName);
    formData.append('lastName', this.user.lastName);
    formData.append('phoneNumber', this.user.phone);
    formData.append('address', this.user.address);
    formData.append('licensePlate', this.vehicle.licensePlate);
    formData.append('vehicleModel', this.vehicle.model);
    formData.append('vehicleType', this.mapVehicleType(this.vehicle.type));
    formData.append('numberOfSeats', String(Number(this.vehicle.seats) || 1));
    formData.append('babyFriendly', String(!!this.vehicle.babyFriendly));
    formData.append('petFriendly', String(!!this.vehicle.petFriendly));
    
    // Add profile image if selected
    if (this.selectedFile) {
      formData.append('profileImage', this.selectedFile, this.selectedFile.name);
    }

    console.log('Posting driver registration with FormData', 'adminId', adminId);
    this.adminService.registerDriver(formData).subscribe({
      next: (res) => {
        console.log('Driver registration response', res);
        this.showToastMessage('Driver registration submitted', 'success');
      },
      error: (err) => {
        console.error('Driver registration failed', err);
        this.showToastMessage('Driver registration failed', 'error');
      }
    });
  }
}

