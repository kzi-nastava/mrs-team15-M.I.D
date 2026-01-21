import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { RideOrderingForm } from '../../components/ride-ordering-form/ride-ordering-form';
import { MapComponent } from '../../../shared/components/map/map';
import { ActiveRideWarningModal } from '../../components/active-ride-warning-modal/active-ride-warning-modal';

@Component({
  selector: 'app-ride-ordering',
  imports: [RideOrderingForm, MapComponent, ActiveRideWarningModal],
  templateUrl: './ride-ordering.html',
  styleUrl: './ride-ordering.css',
})
export class RideOrdering {
  @ViewChild('activeWarning') activeWarning!: ActiveRideWarningModal;

  constructor(private router: Router) {}

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
