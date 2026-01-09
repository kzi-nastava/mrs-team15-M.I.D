import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { FormsModule } from '@angular/forms';
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';

interface FavoriteRoute {
  name: string;
  pickup: string;
  destination: string;
  stops?: string[];
}

@Component({
  selector: 'app-ride-ordering-form',
  standalone: true,
  imports: [Button, InputComponent, CommonModule, FormsModule],
  templateUrl: './ride-ordering-form.html',
  styleUrls: ['./ride-ordering-form.css'],
})
export class RideOrderingForm {
  pickupAddress: string = '';
  destinationAddress: string = '';
  stops: string[] = [];

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

  selectFavorite(name: string) {
    if (name === '') {
      // reset to initial empty state
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

  removeStop(index: number) {
    this.stops.splice(index, 1);
  }

  showRoute() {
    console.log('Show route for', { pickup: this.pickupAddress, destination: this.destinationAddress, stops: this.stops });
  }

  chooseRoute() {
    console.log('Choose route');
  }

}
