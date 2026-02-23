import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// Reusable form component for editing vehicle details
// Used within driver registration and profile forms
@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicle-form.html',
  styleUrl: './vehicle-form.css',
})
export class VehicleForm {
  // Vehicle data passed from parent component
  @Input() vehicle: any = {
    licensePlate: 'NS123AB',
    model: 'Golf 7',
    seats: 4,
    type: 'Standard',
    petFriendly: true,
    babyFriendly: true,
  };

  // Regex pattern for license plate validation
  private licensePlatePattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;

  // Validates license plate format (e.g., NS123AB)
  isLicensePlateValid(licensePlate: string): boolean {
    if (!licensePlate) return false;
    return this.licensePlatePattern.test(licensePlate.toUpperCase());
  }

  // Checks if field is empty
  isFieldEmpty(field: string): boolean {
    return !field || field.trim() === '';
  }

  // Returns error message for license plate validation
  getLicensePlateErrorMessage(): string {
    if (this.isFieldEmpty(this.vehicle.licensePlate)) return 'License plate is required';
    if (!this.isLicensePlateValid(this.vehicle.licensePlate)) return 'License plate format is invalid (e.g: NS123AB)';
    return '';
  }

  // Returns error message for vehicle model validation
  getModelErrorMessage(): string {
    return this.isFieldEmpty(this.vehicle.model) ? 'Car model is required' : '';
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
    return this.isFieldEmpty(this.vehicle.type) ? 'Type is required' : '';
  }

  // Placeholder for vehicle photo upload (not implemented)
  onUploadPhoto(): void {
    console.log('Upload vehicle photo clicked');
  }

  // Placeholder for save action (handled by parent component)
  onSave(): void {
    console.log('Save vehicle', this.vehicle);
  }
}
