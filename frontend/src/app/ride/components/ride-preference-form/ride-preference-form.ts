import { Component, EventEmitter, Output } from '@angular/core';
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

  addEmptyGuest() {
    this.guests.push('');
  }

  removeGuest(idx: number) {
    console.log('removeGuest called, idx=', idx);
    this.guests.splice(idx, 1);
    console.log('guest removed, guests=', this.guests);
  }

  onConfirm() {
    this.confirm.emit({ vehicleType: this.vehicleType, babySeat: this.babySeat, petFriendly: this.petFriendly, guests: [...this.guests] });
  }

  onCancel() {
    this.cancel.emit();
  }
}
