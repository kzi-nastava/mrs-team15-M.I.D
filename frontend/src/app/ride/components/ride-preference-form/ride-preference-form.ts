import { Component, EventEmitter, Output, Input, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { FromValidator } from '../../../shared/components/form-validator';

// Interface for ride preferences data structure
export interface RidePreferences {
  vehicleType: string;
  babySeat: boolean;
  petFriendly?: boolean;
  guests: string[];
  scheduledTime?: string | null;
}

// Form component for selecting ride preferences
// Handles vehicle type, baby seat, pets, guests and scheduled time options
@Component({
  selector: 'app-ride-preference-form',
  standalone: true,
  imports: [CommonModule, FormsModule, InputComponent, Button],
  templateUrl: './ride-preference-form.html',
  styleUrls: ['./ride-preference-form.css'],
})
export class RidePreferenceForm {
  constructor(private cdr: ChangeDetectorRef) {}
  // Route estimate with distance, time and price
  @Input() estimate: any;
  // Vehicle type selected from ordering form
  @Input() selectedVehicleType: string = 'STANDARD';

  // Selected vehicle type (standard/luxury/van)
  vehicleType: string = 'standard';
  // Baby seat requirement flag
  babySeat: boolean = false;
  // Pet friendly requirement flag
  petFriendly: boolean = false;
  // List of guest emails
  guests: string[] = [];
  // Scheduled time for ride (null for immediate)
  scheduledTime: string | null = null;
  // Minimum allowed datetime (now)
  minDatetime: string | null = null;
  // Maximum allowed datetime (5 hours ahead)
  maxDatetime: string | null = null;
  // Error message for scheduled time validation
  scheduledTimeError: string | null = null;
  // Form validator instance
  validator: FromValidator = new FromValidator();

  // TrackBy function for ngFor performance optimization
  trackByIndex(index: number, _item: any) {
    return index;
  }

  // Event emitted when preferences are confirmed
  @Output() confirm = new EventEmitter<RidePreferences>();
  // Event emitted when form is cancelled
  @Output() cancel = new EventEmitter<void>();

  // Handles input changes, initializes min/max datetime values
  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedVehicleType'] && !this.vehicleType) {
      try { this.vehicleType = (this.selectedVehicleType || 'STANDARD').toLowerCase(); } catch(e) {}
    }
    if (changes['estimate']) {
      this.cdr.detectChanges();
    }
    try {
      if (!this.minDatetime) this.minDatetime = this._formatLocalDatetime(new Date());
      if (!this.maxDatetime) this.maxDatetime = this._formatLocalDatetime(new Date(Date.now() + 5 * 60 * 60 * 1000));
    } catch(e) {}
  }

  // Returns formatted price for selected vehicle type
  getSelectedPrice(): string {
    if (!this.estimate) return '-';
    const vt = (this.vehicleType || '').toLowerCase();
    try {
      if (vt === 'standard') {
        return this._formatPrice(this.estimate.priceEstimateStandard ?? this.estimate.priceEstimate ?? null);
      }
      if (vt === 'luxury') {
        return this._formatPrice(this.estimate.priceEstimateLuxury ?? this.estimate.priceEstimate ?? null);
      }
      if (vt === 'van') {
        return this._formatPrice(this.estimate.priceEstimateVan ?? this.estimate.priceEstimate ?? null);
      }
    } catch (e) {
      console.error('Error in getSelectedPrice:', e);
    }
    return this._formatPrice(this.estimate.priceEstimate ?? null);
  }

  // Formats price value to 3 decimal places
  private _formatPrice(val: any): string {
    if (val === null || val === undefined) return '-';
    const n = Number(val);
    if (!isFinite(n)) return '-';
    return n.toFixed(3);
  }

  // Adds new empty guest email field
  addEmptyGuest() {
    this.guests.push('');
  }

  // Handles vehicle type change (placeholder for future logic)
  onVehicleTypeChange(newType: string) {
    
  }

  // Validates and updates scheduled time, checks if in past or too far ahead
  onScheduledTimeChange(val: string | null) {
    this.scheduledTime = val;
    this.scheduledTimeError = null;
    if (this.scheduledTime && this._isInPast(this.scheduledTime)) {
      this.scheduledTimeError = 'Scheduled time cannot be in the past';
    }
    // scheduled time is not more than 5 hours ahead
    try {
      if (this.scheduledTime && this.maxDatetime) {
        const sel = new Date(this.scheduledTime).getTime();
        const max = new Date(this.maxDatetime).getTime();
        if (sel > max) this.scheduledTimeError = 'Scheduled time cannot be more than 5 hours ahead';
      }
    } catch(e) {}
  }

  // Removes guest from list at specified index
  removeGuest(idx: number) {
    console.log('removeGuest called, idx=', idx);
    this.guests.splice(idx, 1);
    console.log('guest removed, guests=', this.guests);
  }

  // Validates form and emits confirm event with preferences
  onConfirm() {
    // validate scheduled time before emitting
    if (this.scheduledTime && this._isInPast(this.scheduledTime)) {
      this.scheduledTimeError = 'Scheduled time cannot be in the past';
      return;
    }
    if (this.scheduledTime && this.maxDatetime) {
      try {
        if (new Date(this.scheduledTime).getTime() > new Date(this.maxDatetime).getTime()) {
          this.scheduledTimeError = 'Scheduled time cannot be more than 5 hours ahead';
          return;
        }
      } catch(e) {}
    }
    const vt = (this.vehicleType || '').toUpperCase() || 'STANDARD';
    this.confirm.emit({ vehicleType: vt, babySeat: this.babySeat, petFriendly: this.petFriendly, guests: [...this.guests], scheduledTime: this.scheduledTime });
  }

  // Emits cancel event to close form
  onCancel() {
    this.cancel.emit();
  }

  // Checks if datetime string is in the past
  private _isInPast(dt: string): boolean {
    try {
      const parsed = new Date(dt);
      const now = new Date();
      return parsed.getTime() < now.getTime();
    } catch (e) { return false; }
  }

  // Formats Date object to datetime-local input format
  private _formatLocalDatetime(d: Date) {
    const pad = (n:number) => String(n).padStart(2, '0');
    const year = d.getFullYear();
    const month = pad(d.getMonth() + 1);
    const day = pad(d.getDate());
    const hours = pad(d.getHours());
    const mins = pad(d.getMinutes());
    return `${year}-${month}-${day}T${hours}:${mins}`;
  }
}
