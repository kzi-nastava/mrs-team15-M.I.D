import { Component, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RideOrderingForm } from '../../components/ride-ordering-form/ride-ordering-form';
import { MapComponent } from '../../../shared/components/map/map';
import { ActiveRideWarningModal } from '../../components/active-ride-warning-modal/active-ride-warning-modal';
import { RideService } from '../../../services/ride.service';

@Component({
  selector: 'app-ride-ordering',
  imports: [RideOrderingForm, MapComponent, ActiveRideWarningModal],
  templateUrl: './ride-ordering.html',
  styleUrl: './ride-ordering.css',
})
export class RideOrdering implements OnInit {
  @ViewChild('activeWarning') activeWarning!: ActiveRideWarningModal;
  hasActiveRide: boolean = false;

  constructor(private router: Router, private rideService: RideService) {}

  ngOnInit(): void {
    // Check if user has an active ride
    this.rideService.getMyCurrentRide().subscribe({
      next: (currentRide) => {
        if (currentRide && currentRide.rideId) {
          // User has an active ride, show the modal
          this.hasActiveRide = true;
          setTimeout(() => {
            this.openActiveRideWarning();
          }, 100);
        }
      },
      error: (err) => {
        // No active ride or error - user can order normally
        this.hasActiveRide = false;
        console.debug('No active ride found or error checking:', err);
      }
    });
  }

  // Call this method to show the active-ride warning modal
  openActiveRideWarning(): void {
    if (this.activeWarning) {
      this.activeWarning.openModal();
    }
  }

  // Handle order attempt event payload: navigate to finding-driver and pass order response
  handleOrderAttempt(eventPayload: any) {
    // RideOrderingForm now navigates immediately before calling backend.
    // This handler receives server response updates (if any) and can react accordingly.
    try {
      console.debug('Order attempt event received (server response or update):', eventPayload);
      // If backend reported an error, show the active ride warning modal
      if (eventPayload && eventPayload.error) {
        console.warn('Order failed:', eventPayload.error);
        this.openActiveRideWarning();
      }
    } catch (e) {
      console.warn('handleOrderAttempt processing error', e);
    }
  }

}
