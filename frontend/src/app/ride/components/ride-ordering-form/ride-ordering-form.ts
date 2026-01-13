import { Component, EventEmitter, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { FormsModule } from '@angular/forms';
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { RidePreferenceForm } from '../ride-preference-form/ride-preference-form';
import { RideService } from '../../../services/ride.service';

interface FavoriteRoute {
  name: string;
  pickup: string;
  destination: string;
  stops?: string[];
}

@Component({
  selector: 'app-ride-ordering-form',
  standalone: true,
  imports: [Button, InputComponent, CommonModule, FormsModule, RidePreferenceForm],
  templateUrl: './ride-ordering-form.html',
  styleUrls: ['./ride-ordering-form.css'],
})
export class RideOrderingForm {
  pickupAddress: string = '';
  destinationAddress: string = '';
  stops: string[] = [];
  draggedIndex: number | null = null;
  dragOverIndex: number | null = null;

  favorites: FavoriteRoute[] = [
    { name: 'Home → Work', pickup: '123 Home St', destination: '456 Work Ave', stops: [] },
    { name: 'Airport Ride', pickup: 'Home Address', destination: 'Airport Terminal 1', stops: [] },
  ];
  selectedFavorite: string | null = null;
  favoriteOpen: boolean = false;

  validator: FromValidator = new FromValidator();

  hasErrors(): boolean {
    if (this.validator.addressError(this.pickupAddress) || this.validator.addressError(this.destinationAddress)) return true;
    for (const s of this.stops) {
      if (this.validator.addressError(s)) return true;
    }
    return false;
  }

  applyFavorite() {
    const fav = this.favorites.find(f => f.name === this.selectedFavorite);
    if (!fav) return;
    this.pickupAddress = fav.pickup;
    this.destinationAddress = fav.destination;
    this.stops = fav.stops ? [...fav.stops] : [];
  }

  toggleFavoriteDropdown() {
    this.favoriteOpen = !this.favoriteOpen;
  }

  trackByIndex(index: number, _item: any) {
    return index;
  }

  selectFavorite(name: string) {
    if (name === '') {
      this.selectedFavorite = '';
      this.pickupAddress = '';
      this.destinationAddress = '';
      this.stops = [];
    } else {
      this.selectedFavorite = name;
      this.applyFavorite();
    }
    this.favoriteOpen = false;
  }

  addStop() {
    this.stops.push('');
  }

  onDragStart(event: DragEvent, index: number) {
    this.draggedIndex = index;
    try { event.dataTransfer?.setData('text/plain', String(index)); } catch (e) {}
    if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move';
  }

  onDragOver(event: DragEvent, index: number) {
    event.preventDefault();
    this.dragOverIndex = index;
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'move';
  }

  onDragLeave(_event: DragEvent, _index: number) {
    this.dragOverIndex = null;
  }

  onDrop(event: DragEvent, index: number) {
    event.preventDefault();
    const from = this.draggedIndex !== null ? this.draggedIndex : Number(event.dataTransfer?.getData('text/plain'));
    const to = index;
    if (from === to || from == null) {
      this.dragOverIndex = null;
      this.draggedIndex = null;
      return;
    }
    const item = this.stops.splice(from, 1)[0];
    // If dragging from a position before the drop target and we removed it, the target index shifts down by 1
    const insertIndex = from < to ? to : to;
    this.stops.splice(insertIndex, 0, item);
    this.dragOverIndex = null;
    this.draggedIndex = null;
  }

  onDragEnd(_event: DragEvent) {
    this.dragOverIndex = null;
    this.draggedIndex = null;
  }

  removeStop(index: number) {
    this.stops.splice(index, 1);
  }

  showRoute() {
    console.log('Show route for', { pickup: this.pickupAddress, destination: this.destinationAddress, stops: this.stops });
  }

  showPreferences: boolean = false;

  chooseRoute() {
    this.showPreferences = true;
  }

  constructor(private rideService: RideService) {}

  async onPreferencesConfirm(prefs: any) {
    console.log('Preferences confirmed from form:', prefs);
    this.showPreferences = false;

    // Build basic estimate payload by geocoding addresses
    try {
      const startGeo = await this.rideService.geocodeAddress(this.pickupAddress) || { lat: 0, lon: 0 };
      const endGeo = await this.rideService.geocodeAddress(this.destinationAddress) || { lat: 0, lon: 0 };

      const stopAddresses = this.stops.filter(s => s && s.trim().length > 0);
      const stopLatitudes: number[] = [];
      const stopLongitudes: number[] = [];
      for (const s of stopAddresses) {
        const g = await this.rideService.geocodeAddress(s);
        if (g) {
          stopLatitudes.push(g.lat);
          stopLongitudes.push(g.lon);
        }
      }

      const estimateReq = {
        startAddress: this.pickupAddress,
        startLatitude: startGeo.lat,
        startLongitude: startGeo.lon,
        endAddress: this.destinationAddress,
        endLatitude: endGeo.lat,
        endLongitude: endGeo.lon,
        stopAddresses: stopAddresses,
        stopLatitudes: stopLatitudes,
        stopLongitudes: stopLongitudes,
      };

      const route = await this.rideService.estimateRoute(estimateReq);
      console.log('Route estimate', route);

      const userJson = localStorage.getItem('user');
      let mainEmail = 'guest@example.com';
      try { if (userJson) mainEmail = JSON.parse(userJson).email || mainEmail; } catch(e) {}

      const orderDto: any = {
        mainPassengerEmail: mainEmail,
        startAddress: estimateReq.startAddress,
        startLatitude: estimateReq.startLatitude,
        startLongitude: estimateReq.startLongitude,
        endAddress: estimateReq.endAddress,
        endLatitude: estimateReq.endLatitude,
        endLongitude: estimateReq.endLongitude,
        stopAddresses: estimateReq.stopAddresses,
        stopLatitudes: estimateReq.stopLatitudes,
        stopLongitudes: estimateReq.stopLongitudes,
        vehicleType: prefs.vehicleType || 'STANDARD',
        babyFriendly: !!prefs.babySeat,
        petFriendly: !!prefs.petFriendly,
        linkedPassengers: prefs.guests && prefs.guests.length ? prefs.guests : [],
        scheduledTime: null,
        distanceKm: route?.distanceKm ?? 0,
        estimatedTimeMinutes: route?.estimatedTimeMinutes ?? (route?.estimatedDurationMin ?? 0),
        priceEstimate: route?.priceEstimate ?? 0,
      };

      console.log('Order DTO', orderDto);
      const res = await this.rideService.orderRide(orderDto);
      console.log('Order response', res);

      // Notify parent that an order attempt was made (parent may show active-ride modal)
      this.orderAttempt.emit(res);

    } catch (err) {
      console.error('Order failed', err);
      this.orderAttempt.emit({ error: err });
    }
  }

  @Output() orderAttempt = new EventEmitter<any>();

}
