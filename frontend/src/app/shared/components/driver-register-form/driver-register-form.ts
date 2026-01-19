import { Component, ViewChild, ElementRef, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Button } from '../button/button';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-driver-register-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, Button],
  templateUrl: './driver-register-form.html',
  styleUrl: './driver-register-form.css',
})
export class DriverRegisterForm implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  userAvatar: string = '';
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  user = {
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    email: '',
    role: 'driver',
    activeHours: 0,
  };

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
    // check if user is admin
  const role = localStorage.getItem('role');
  if (role !== 'ADMIN') {
    this.showToastMessage('Access denied', 'error');
    return;
  }
}

  private emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  private phonePattern = /^(\+381|0)[0-9]{9,10}$/;

  private licensePlatePattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;

  isLicensePlateValid(licensePlate: string): boolean {
    if (!licensePlate) return false;
    return this.licensePlatePattern.test(licensePlate.toUpperCase());
  }

  isVehicleFieldEmpty(field: string): boolean {
    return !field || field.trim() === '';
  }

  getLicensePlateErrorMessage(): string {
    if (this.isVehicleFieldEmpty(this.vehicle.licensePlate)) return 'License plate is required';
    if (!this.isLicensePlateValid(this.vehicle.licensePlate)) return 'License plate format is invalid (e.g: NS123AB)';
    return '';
  }

  getModelErrorMessage(): string {
    return this.isVehicleFieldEmpty(this.vehicle.model) ? 'Car model is required' : '';
  }

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

  getTypeErrorMessage(): string {
    return this.isVehicleFieldEmpty(this.vehicle.type) ? 'Type is required' : '';
  }

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

    const payload: any = {
      email: this.user.email,
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      phoneNumber: this.user.phone,
      address: this.user.address,
      profileImage: this.userAvatar || null,
      licensePlate: this.vehicle.licensePlate,
      vehicleModel: this.vehicle.model,
      vehicleType: this.mapVehicleType(this.vehicle.type),
      numberOfSeats: Number(this.vehicle.seats) || 1,
      babyFriendly: !!this.vehicle.babyFriendly,
      petFriendly: !!this.vehicle.petFriendly,
    };

    console.log('Posting driver registration payload', payload, 'adminId', adminId);
    this.adminService.registerDriver(payload).subscribe({
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

