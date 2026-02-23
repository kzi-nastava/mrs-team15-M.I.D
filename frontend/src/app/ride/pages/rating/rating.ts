import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { StarRating } from '../../../shared/components/star-rating/star-rating';
import { Button } from '../../../shared/components/button/button';
import { RideService } from '../../../services/ride.service';

@Component({
  selector: 'app-rating',
  imports: [CommonModule, FormsModule, StarRating, Button],
  templateUrl: './rating.html',
  styleUrl: './rating.css',
})
export class Rating implements OnInit {
  driverRating: number = 0;
  vehicleRating: number = 0;
  driverComment: string = '';
  vehicleComment: string = '';
  rideId: number = 0;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private rideService: RideService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.rideId = +params['id']; // Convert string to number
    });
  }

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

    const ratingData = {
      driverRating: this.driverRating,
      vehicleRating: this.vehicleRating,
      driverComment: this.driverComment,
      vehicleComment: this.vehicleComment
    };

    // Call the RideService to submit the rating for the specific ride ID,
    // handles success and error responses accordingly
    this.rideService.rateRide(this.rideId, ratingData).subscribe({
      next: (response) => {
        console.log('Rating submitted successfully', response);
        this.router.navigate(['/ride-ordering']);
      },
      error: (error) => {
        console.error('Error submitting rating', error);
        alert('Failed to submit rating. Please try again.');
      }
    });
  }

  skipRating() {
    this.router.navigate(['/home']);
  }
}
