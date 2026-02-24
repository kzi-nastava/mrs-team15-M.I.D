import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../../../shared/components/map/map';
import { CurrentRideForm } from '../../components/current-ride-form/current-ride-form';
import { formatAddress } from '../../../shared/utils/address.utils';

@Component({
  selector: 'app-current-ride',
  imports: [MapComponent, CurrentRideForm, CommonModule],
  templateUrl: './current-ride.html',
  styleUrl: './current-ride.css',
})
export class CurrentRide implements OnInit {
  ride: any = null;
  isAdminView = false;
  estimatedDistanceKm = 0;
  remainingTimeMin = 0;

  constructor(private router: Router) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state) {
      this.ride = navigation.extras.state['ride'];
      this.isAdminView = navigation.extras.state['fromAdmin'] || false;

      if (this.ride && this.isAdminView) {
        this.calculateRideInfo();
      }
    }
  }

  private calculateRideInfo(): void {
    // Mock distance calculation (you can replace with actual calculation)
    this.estimatedDistanceKm = this.ride.estimatedDuration * 0.8; // Rough estimate

    // Calculate remaining time
    if (this.ride.startTime) {
      const start = new Date(this.ride.startTime);
      const now = new Date();
      const elapsedMinutes = Math.floor((now.getTime() - start.getTime()) / (1000 * 60));
      this.remainingTimeMin = Math.max(0, this.ride.estimatedDuration - elapsedMinutes);
    } else {
      this.remainingTimeMin = this.ride.estimatedDuration;
    }
  }

  getFormattedRoute(): { start: string; end: string } {
    if (!this.ride) return { start: '', end: '' };
    return {
      start: formatAddress(this.ride.startAddress),
      end: formatAddress(this.ride.endAddress)
    };
  }
}
