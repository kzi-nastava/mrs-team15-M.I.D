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
    try {
      console.debug('Order attempt received, navigating to finding-driver with payload', eventPayload);
      // Navigate to finding-driver and include the backend response in navigation state
      this.router.navigate(['/finding-driver'], { state: { order: eventPayload } });
    } catch (e) {
      console.warn('Navigation to finding-driver failed', e);
      // fallback: open active ride warning modal if navigation fails
      this.openActiveRideWarning();
    }
  }

}
