import { Component, EventEmitter, Output, Input, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { FromValidator } from '../../../shared/components/form-validator';

// Interface za preferencije vožnje
export interface RidePreferences {
  vehicleType: string;
  babySeat: boolean;
  petFriendly?: boolean;
  guests: string[];
  scheduledTime?: string | null;
}

// Forma komponenta za izbor preferencija vožnje (tip vozila, dodatne opcije, zakazivanje)
@Component({
  selector: 'app-ride-preference-form',
  standalone: true,
  imports: [CommonModule, FormsModule, InputComponent, Button],
  templateUrl: './ride-preference-form.html',
  styleUrls: ['./ride-preference-form.css'],
})
export class RidePreferenceForm {
  constructor(private cdr: ChangeDetectorRef) {}
  // Estimate objekat sa cenom i podacima o ruti
  @Input() estimate: any;
  // Prethodno selektovan tip vozila
  @Input() selectedVehicleType: string = 'STANDARD';

  // Tip vozila (standard/luxury/van)
  vehicleType: string = 'standard';
  // Da li je potrebno bebi sedište
  babySeat: boolean = false;
  // Da li je vozilo pet-friendly
  petFriendly: boolean = false;
  // Lista email adresa gostiju
  guests: string[] = [];
  // Vreme zakazane vožnje
  scheduledTime: string | null = null;
  // Minimalno vreme za zakazivanje
  minDatetime: string | null = null;
  // Maksimalno vreme za zakazivanje (5h unapred)
  maxDatetime: string | null = null;
  // Error poruka za scheduled time
  scheduledTimeError: string | null = null;
  // Validator za email adrese
  validator: FromValidator = new FromValidator();

  trackByIndex(index: number, _item: any) {
    return index;
  }

  // Event emitter za potvrdu preferencija
  @Output() confirm = new EventEmitter<RidePreferences>();
  // Event emitter za otkazivanje
  @Output() cancel = new EventEmitter<void>();

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

  // Vraća cenu za trenutno selektovan tip vozila
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

  // Formatira cenu u string sa 3 decimale
  private _formatPrice(val: any): string {
    if (val === null || val === undefined) return '-';
    const n = Number(val);
    if (!isFinite(n)) return '-';
    return n.toFixed(3);
  }

  // Dodaje prazno polje za guest email
  addEmptyGuest() {
    this.guests.push('');
  }

  onVehicleTypeChange(newType: string) {
    
  }

  // Handler za promenu scheduled time sa validacijom
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

  // Uklanja gosta sa zadatog indexa
  removeGuest(idx: number) {
    console.log('removeGuest called, idx=', idx);
    this.guests.splice(idx, 1);
    console.log('guest removed, guests=', this.guests);
  }

  // Potvrđuje preferencije i emituje event
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
