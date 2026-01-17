import { ChangeDetectorRef, Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { RideService } from '../../../services/ride.service';
import { Router } from '@angular/router';
import { MapRouteService } from '../../../services/map-route.service';

@Component({
  selector: 'app-ride-estimation-form',
  imports: [Button, InputComponent, CommonModule],
  templateUrl: './ride-estimation-form.html',
  styleUrl: './ride-estimation-form.css',
})
export class RideEstimationForm {
  constructor(private cdr: ChangeDetectorRef, private rideService : RideService, private mapRouteService : MapRouteService, private router : Router){}


  pickupAddress : string = '';
  destinationAddress : string = '';
  message = '';
  showMessage = false;
  showEstimationInfo: boolean = false;
  estimatedDistanceKm?: number;
  estimatedDurationMin?: number;


  validator : FromValidator = new FromValidator();

  hasErrors() : boolean {
    return !!(this.validator.addressError(this.pickupAddress) || this.validator.addressError(this.destinationAddress));
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();  
    setTimeout(() => { this.showMessage = false;}, 3000);
  }

  estimateRide() {
    if (this.hasErrors()) return;

    const data = {
      startAddress: this.pickupAddress,
      destinationAddress: this.destinationAddress
    };

    this.rideService.estimate(data).subscribe({
      next: (response) => {
        this.showEstimationInfo = true;
        this.estimatedDistanceKm = response.distanceKm;
        this.estimatedDurationMin = response.estimatedDurationMin
        this.cdr.detectChanges();
        this.mapRouteService.drawRoute(response.route);
      },
      error: (err) => {
        if (typeof err.error === 'string') {
          this.showMessageToast(err.error);
        } else {
          this.showMessageToast('Ride estimation failed. Please try again.');
        }
      }
    });
  }
}