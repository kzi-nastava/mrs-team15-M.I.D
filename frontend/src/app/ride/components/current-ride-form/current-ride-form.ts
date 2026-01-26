import { Router } from '@angular/router';
import { ChangeDetectorRef, Component, ViewChild, OnDestroy } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { ReportInconsistencyModal } from '../report-inconsistency-modal/report-inconsistency-modal';
import { StopRideModal } from '../stop-ride-modal/stop-ride-modal';
import { PanicModal } from '../panic-modal/panic-modal';
import { RideService } from '../../../services/ride.service';
import { MapRouteService } from '../../../services/map-route.service';
import { Subscription, interval } from 'rxjs';


export interface CurrentRideDTO {
  rideId?: number;
  estimatedDurationMin: number;
  distanceKm: number;
  route: any;
  startAddress: string;
  endAddress: string;
}


@Component({
  selector: 'app-current-ride-form',
  imports: [Button, CommonModule, ReportInconsistencyModal, StopRideModal, PanicModal],
  templateUrl: './current-ride-form.html',
  styleUrl: './current-ride-form.css',
})

export class CurrentRideForm implements OnDestroy {
  @ViewChild(ReportInconsistencyModal) reportModal!: ReportInconsistencyModal;

  constructor(
    private cdr: ChangeDetectorRef,
    private rideService: RideService,
    private mapRouteService: MapRouteService,
    private router: Router
  ){}

  pickupAddress : string = '';
  destinationAddress : string = '';
  message = '';
  showMessage = false;
  estimatedDistanceKm?: number;
  estimatedDurationMin?: number;
  remainingTimeMin?: number;
  rideId?: number;
  private trackingSubscription?: Subscription;

  isDriver: boolean = false;
  isPassenger: boolean = true;

  showStopModal: boolean = false;
  showPanicModal: boolean = false;


  ngOnInit(): void {
    const role = localStorage.getItem('role');
    if(role == "DRIVER"){
      this.isDriver = true;
      this.isPassenger = false;
    }else{
      this.isDriver = false;
      this.isPassenger = true;
    }
    // If navigation provided ride info in state, initialize from it to avoid extra API call
    try {
      const nav = (this.router as any).getCurrentNavigation && (this.router as any).getCurrentNavigation();
      const incoming = nav && nav.extras && nav.extras.state && (nav.extras.state.ride || nav.extras.state.order) ? (nav.extras.state.ride || nav.extras.state.order) : (typeof history !== 'undefined' && (history as any).state ? (history as any).state.ride || (history as any).state.order : null);
      if (incoming) {
        try {
          this.pickupAddress = incoming.startAddress || incoming.startAddress || '';
          this.destinationAddress = incoming.endAddress || incoming.endAddress || '';
          this.estimatedDistanceKm = incoming.distanceKm || incoming.distanceKm;
          this.estimatedDurationMin = incoming.estimatedTimeMinutes || incoming.estimatedDurationMin || incoming.estimatedDurationMin;
          this.rideId = incoming.id || incoming.rideId || undefined;
          // draw route immediately if provided
          if (incoming.route) {
            try { this.mapRouteService.drawRoute(incoming.route); } catch(e) {}
          } else if (incoming.routeLattitudes && incoming.routeLongitudes && Array.isArray(incoming.routeLattitudes) && Array.isArray(incoming.routeLongitudes) && incoming.routeLattitudes.length === incoming.routeLongitudes.length) {
            const pts = incoming.routeLattitudes.map((lat:any, i:number) => ({ lat: Number(lat), lng: Number(incoming.routeLongitudes[i]) }));
            try { this.mapRouteService.drawRoute(pts); } catch(e) {}
          }
          this.cdr.detectChanges();
          // if we have a rideId, start tracking
          if (this.rideId) {
            this.startTracking(Number(this.rideId));
          }
          return;
        } catch (e) {
          console.warn('Initializing CurrentRideForm from navigation state failed', e);
        }
      }
    } catch (e) {
      console.warn('Checking navigation state for current ride failed', e);
    }

    // fallback: fetch from backend
    this.fetchCurrentRide();
  }

 fetchCurrentRide(): void {
  this.rideService.getMyCurrentRide().subscribe({
    next: (response) => {
      this.destinationAddress = response.endAddress;
      this.pickupAddress = response.startAddress;
      this.estimatedDistanceKm = response.distanceKm;
      this.estimatedDurationMin = response.estimatedDurationMin;
      this.rideId = response.rideId;
      this.cdr.detectChanges();
      this.mapRouteService.drawRoute(response.route);

      if (this.rideId) {
        this.startTracking(this.rideId);
      }
    },
    error: (err) => {
      if (typeof err.error === 'string') {
        this.showMessageToast(err.error);
      } else {
        this.showMessageToast('Getting current ride data failed. Please try again.');
      }
    }
  });
}
  openReportModal() {
    this.reportModal.openModal();
  }

  handleReportSubmitted(description: string) {
    if (!this.rideId) {
      this.showMessageToast('Ride ID not available. Please try again.');
      return;
    }

    const reportData = {
      rideId: this.rideId,
      description: description
    };

    this.rideService.reportInconsistency(reportData).subscribe({
      next: (response) => {
        this.showMessageToast('Inconsistency reported successfully.');
      },
      error: (err) => {
        if (typeof err.error === 'string') {
          this.showMessageToast(err.error);
        } else {
          this.showMessageToast('Failed to report inconsistency. Please try again.' + err.message);
        }
      }
    });
  }

  openStopModal(): void {
    this.showStopModal = true;
  }

  finalPrice?: number;
  onStopConfirmed(response: any) {
    this.estimatedDistanceKm = response.distanceKm;
    this.estimatedDurationMin = response.estimatedDurationMin;
    this.destinationAddress = response.endAddress;
    this.finalPrice = response.price;
    this.mapRouteService.drawRoute(response.route);
    this.showMessageToast(`Ride completed!`);
    this.showStopModal = false;
    this.cdr.detectChanges();
  }

  openPanicModal(): void {
    this.showPanicModal = true;
  }

  onPanicConfirmed() {
    this.showPanicModal = false;
    this.mapRouteService.alertRoute();
  }

  markCompleted(): void {
    if (!this.rideId) {
      this.showMessageToast('Ride ID not available.');
      return;
    }

    this.rideService.finishRide(this.rideId).subscribe({
      next: (hasNextRide) => {
        if (hasNextRide) {
          this.showMessageToast('Ride marked as completed. Loading next ride...');
          this.fetchCurrentRide();
        } else {
          this.showMessageToast('Ride marked as completed.');
          this.router.navigate(['/upcoming-rides']);
        }
      },
      error: (err) => {
        let message = 'Failed to mark ride as completed. Please try again.';
        if (typeof err.error === 'string') {
          message = err.error;
        } else if (err.error?.message) {
          message = err.error.message;
        }
        this.showMessageToast(message);
      }
    });
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false;}, 3000);
  }

  private startTracking(rideId: number): void {
    if (this.trackingSubscription) {
      this.trackingSubscription.unsubscribe();
    }

    // Fetch immediately
    this.rideService.trackRide(rideId).subscribe({
      next: (trackData) => {
        this.remainingTimeMin = trackData.remainingTimeInMinutes;
        this.mapRouteService.updateVehicleLocation(
          trackData.location.latitude,
          trackData.location.longitude
        );
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error tracking ride:', err);
      }
    });

    // Then continue fetching every 10 seconds
    this.trackingSubscription = interval(10000).subscribe(() => {
      this.rideService.trackRide(rideId).subscribe({
        next: (trackData) => {
          this.remainingTimeMin = trackData.remainingTimeInMinutes;
          this.mapRouteService.updateVehicleLocation(
            trackData.location.latitude,
            trackData.location.longitude
          );
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error tracking ride:', err);
        }
      });
    });
  }

  ngOnDestroy(): void {
    if (this.trackingSubscription) {
      this.trackingSubscription.unsubscribe();
    }
    this.mapRouteService.clearVehicleLocation();
  }
}
