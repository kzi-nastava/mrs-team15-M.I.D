import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { StarRating } from '../../../shared/components/star-rating/star-rating';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-rating',
  imports: [CommonModule, FormsModule, StarRating, Button],
  templateUrl: './rating.html',
  styleUrl: './rating.css',
})
export class Rating {
  driverRating: number = 0;
  vehicleRating: number = 0;
  driverComment: string = '';
  vehicleComment: string = '';

  constructor(private router: Router) {}

  onDriverRatingChange(rating: number) {
    this.driverRating = rating;
  }

  onVehicleRatingChange(rating: number) {
    this.vehicleRating = rating;
  }

  isRatingValid(): boolean {
    return this.driverRating > 0 && this.vehicleRating > 0;
  }

  submitRating() {
    if (!this.isRatingValid()) {
      alert('Please rate both the driver and the vehicle before submitting.');
      return;
    }

    console.log('Driver Rating:', this.driverRating);
    console.log('Driver Comment:', this.driverComment);
    console.log('Vehicle Rating:', this.vehicleRating);
    console.log('Vehicle Comment:', this.vehicleComment);
    // TODO: Implement API call to submit ratings
    this.router.navigate(['/home']);
  }

  skipRating() {
    this.router.navigate(['/home']);
  }
}
