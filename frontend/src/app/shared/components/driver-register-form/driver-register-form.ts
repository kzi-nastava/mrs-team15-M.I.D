import { Component, ViewChild, ElementRef, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Button } from '../button/button';

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

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {}

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

