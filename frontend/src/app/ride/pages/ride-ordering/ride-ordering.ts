import { Component, ViewChild } from '@angular/core';
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

  // Call this method to show the active-ride warning modal
  openActiveRideWarning(): void {
    if (this.activeWarning) {
      this.activeWarning.openModal();
    }
  }

}
