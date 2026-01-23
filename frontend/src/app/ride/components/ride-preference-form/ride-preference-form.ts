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
  validator: FromValidator = new FromValidator();

  trackByIndex(index: number, _item: any) {
    return index;
  }

  @Output() confirm = new EventEmitter<RidePreferences>();
  @Output() cancel = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedVehicleType'] && !this.vehicleType) {
      // map parent uppercase value to lowercase option values used in this form
      try { this.vehicleType = (this.selectedVehicleType || 'STANDARD').toLowerCase(); } catch(e) {}
    }
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

  removeGuest(idx: number) {
    console.log('removeGuest called, idx=', idx);
    this.guests.splice(idx, 1);
    console.log('guest removed, guests=', this.guests);
  }

  onConfirm() {
    const vt = (this.vehicleType || '').toUpperCase() || 'STANDARD';
    this.confirm.emit({ vehicleType: vt, babySeat: this.babySeat, petFriendly: this.petFriendly, guests: [...this.guests] });
  }

  onCancel() {
    this.cancel.emit();
  }
}
