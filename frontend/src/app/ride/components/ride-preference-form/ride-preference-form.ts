import { Component, EventEmitter, Output, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { FromValidator } from '../../../shared/components/form-validator';

export interface RidePreferences {
  vehicleType: string;
  babySeat: boolean;
  petFriendly?: boolean;
  guests: string[];
  scheduledTime?: string | null;
}

@Component({
  selector: 'app-ride-preference-form',
  standalone: true,
  imports: [CommonModule, FormsModule, InputComponent, Button],
  templateUrl: './ride-preference-form.html',
  styleUrls: ['./ride-preference-form.css'],
})
export class RidePreferenceForm {
  constructor() {}
  @Input() estimate: any;
  @Input() selectedVehicleType: string = 'STANDARD';

  vehicleType: string = '';
  babySeat: boolean = false;
  petFriendly: boolean = false;
  guests: string[] = [];
  scheduledTime: string | null = null;
  minDatetime: string | null = null;
  maxDatetime: string | null = null;
  scheduledTimeError: string | null = null;
  validator: FromValidator = new FromValidator();

  trackByIndex(index: number, _item: any) {
    return index;
  }

  @Output() confirm = new EventEmitter<RidePreferences>();
  @Output() cancel = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedVehicleType'] && !this.vehicleType) {
      try { this.vehicleType = (this.selectedVehicleType || 'STANDARD').toLowerCase(); } catch(e) {}
    }
    try {
      if (!this.minDatetime) this.minDatetime = this._formatLocalDatetime(new Date());
      if (!this.maxDatetime) this.maxDatetime = this._formatLocalDatetime(new Date(Date.now() + 5 * 60 * 60 * 1000));
    } catch(e) {}
  }

  getSelectedPrice(): string {
    if (!this.estimate) return '-';
    const vt = (this.vehicleType || '').toLowerCase();
    try {
      if (vt === 'standard') return String(this.estimate.priceEstimateStandard ?? this.estimate.priceEstimate ?? '-');
      if (vt === 'luxury') return String(this.estimate.priceEstimateLuxury ?? this.estimate.priceEstimate ?? '-');
      if (vt === 'van') return String(this.estimate.priceEstimateVan ?? this.estimate.priceEstimate ?? '-');
    } catch (e) {}
    return String(this.estimate.priceEstimate ?? '-');
  }

  addEmptyGuest() {
    this.guests.push('');
  }

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

  removeGuest(idx: number) {
    console.log('removeGuest called, idx=', idx);
    this.guests.splice(idx, 1);
    console.log('guest removed, guests=', this.guests);
  }

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

  onCancel() {
    this.cancel.emit();
  }

  private _isInPast(dt: string): boolean {
    try {
      const parsed = new Date(dt);
      const now = new Date();
      return parsed.getTime() < now.getTime();
    } catch (e) { return false; }
  }

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
