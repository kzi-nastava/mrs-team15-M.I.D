import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';


// Reusable forma komponenta za unos i prikaz podataka o vozilu
@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicle-form.html',
  styleUrl: './vehicle-form.css',
})
export class VehicleForm {
  // Input objekat sa podacima o vozilu
  @Input() vehicle: any = {
    licensePlate: 'NS123AB',
    model: 'Golf 7',
    seats: 4,
    type: 'Standard',
    petFriendly: true,
    babyFriendly: true,
  };

  private licensePlatePattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;

  // Validira format registarske tablice
  isLicensePlateValid(licensePlate: string): boolean {
    if (!licensePlate) return false;
    return this.licensePlatePattern.test(licensePlate.toUpperCase());
  }

  isFieldEmpty(field: string): boolean {
    return !field || field.trim() === '';
  }

  // Vraća error poruku za registarsku tablicu
  getLicensePlateErrorMessage(): string {
    if (this.isFieldEmpty(this.vehicle.licensePlate)) return 'License plate is required';
    if (!this.isLicensePlateValid(this.vehicle.licensePlate)) return 'License plate format is invalid (e.g: NS123AB)';
    return '';
  }

  // Vraća error poruku za model vozila
  getModelErrorMessage(): string {
    return this.isFieldEmpty(this.vehicle.model) ? 'Car model is required' : '';
  }

  // Vraća error poruku za broj sedišta
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
    return this.isFieldEmpty(this.vehicle.type) ? 'Type is required' : '';
  }

  onUploadPhoto(): void {
    console.log('Upload vehicle photo clicked');
  }

  onSave(): void {
    console.log('Save vehicle', this.vehicle);
  }
}
