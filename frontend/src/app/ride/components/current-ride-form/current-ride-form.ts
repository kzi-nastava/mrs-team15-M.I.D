import { Router } from '@angular/router';
import { ChangeDetectorRef, Component, ViewChild, OnDestroy, Input } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { ReportInconsistencyModal } from '../report-inconsistency-modal/report-inconsistency-modal';
import { StopRideModal } from '../stop-ride-modal/stop-ride-modal';
import { PanicModal } from '../panic-modal/panic-modal';
import { CompleteRideModal } from '../complete-ride-modal/complete-ride-modal';
import { RideService } from '../../../services/ride.service';
import { MapRouteService } from '../../../services/map-route.service';
import { Subscription, interval } from 'rxjs';
import { DriverService } from '../../../services/driver.service';
import { formatAddress } from '../../../shared/utils/address.utils';


export interface RouteDTO {
  distanceKm: number;
  estimatedTimeMin: number;
  startLocation: { latitude: number; longitude: number; address: string };
  endLocation: { latitude: number; longitude: number; address: string };
  stopLocations: { latitude: number; longitude: number; address: string }[];
  polylinePoints: { latitude: number; longitude: number }[];
}

export interface CurrentRideDTO {
  rideId?: number;
  estimatedDurationMin: number;
  route: RouteDTO;
}


@Component({
  selector: 'app-current-ride-form',
  imports: [Button, CommonModule, ReportInconsistencyModal, StopRideModal, PanicModal, CompleteRideModal],
  templateUrl: './current-ride-form.html',
  styleUrl: './current-ride-form.css',
})

export class CurrentRideForm implements OnDestroy {
  @ViewChild(ReportInconsistencyModal) reportModal!: ReportInconsistencyModal;
  @Input() rideData: any;

  constructor(
    private cdr: ChangeDetectorRef,
    private rideService: RideService,
    private mapRouteService: MapRouteService,
    private router: Router,
    private driverService : DriverService
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
  isAdmin: boolean = false;

  // Driver and passenger information for admin view
  driverName?: string;
  passengerNames?: string;

  showStopModal: boolean = false;
  showPanicModal: boolean = false;
  showCompleteModal: boolean = false;


  ngOnInit(): void {
    this.mapRouteService.clearRoute();

    // Check if accessed from admin panel via navigation state
    const nav = (this.router as any).getCurrentNavigation && (this.router as any).getCurrentNavigation();
    const fromAdmin = nav && nav.extras && nav.extras.state && nav.extras.state.fromAdmin;

    const role = localStorage.getItem('role');
    if(fromAdmin || role === "ADMIN"){
      this.isAdmin = true;
      this.isDriver = false;
      this.isPassenger = false;
    } else if(role == "DRIVER"){
      this.isDriver = true;
      this.isPassenger = false;
      // Fetch driver status to trigger location tracking
      this.driverService.getMyStatus().subscribe();
    }else{
      this.isDriver = false;
      this.isPassenger = true;
    }
    // If navigation provided ride info in state, initialize from it to avoid extra API call
    try {
      const nav = (this.router as any).getCurrentNavigation && (this.router as any).getCurrentNavigation();
      const incoming = this.rideData || (nav && nav.extras && nav.extras.state && (nav.extras.state.ride || nav.extras.state.order) ? (nav.extras.state.ride || nav.extras.state.order) : (typeof history !== 'undefined' && (history as any).state ? (history as any).state.ride || (history as any).state.order : null));
      if (incoming) {
        try {
          // Handle new route structure (from current ride or admin active rides)
          if (incoming.route && incoming.route.startLocation && incoming.route.endLocation) {
            this.pickupAddress = formatAddress(incoming.route.startLocation.address);
            this.destinationAddress = formatAddress(incoming.route.endLocation.address);
            this.estimatedDistanceKm = incoming.route.distanceKm;
            this.estimatedDurationMin = incoming.route.estimatedTimeMin || incoming.estimatedDurationMin;
            this.rideId = incoming.rideId || incoming.id || undefined;

            // Extract driver and passenger info for admin view
            if (this.isAdmin && incoming.driverName) {
              this.driverName = incoming.driverName;
              this.passengerNames = incoming.passengerNames;
            }

            // Draw route from polylinePoints
            if (incoming.route.polylinePoints && incoming.route.polylinePoints.length > 0) {
              const routePoints = incoming.route.polylinePoints.map((p: any) => ({ lat: p.latitude, lng: p.longitude }));
              try { this.mapRouteService.drawRoute(routePoints); } catch(e) {}

              // Draw stop markers
              if (incoming.route.stopLocations && incoming.route.stopLocations.length > 0) {
                const stopPoints = incoming.route.stopLocations.map((s: any) => ({
                  lat: s.latitude,
                  lng: s.longitude,
                  name: formatAddress(s.address)
                }));
                try { this.mapRouteService.drawMarkers(stopPoints); } catch(e) {}
              }
            }
          } else {
            // Fallback for old format or admin ActiveRide format
            this.pickupAddress = formatAddress(incoming.startAddress || '');
            this.destinationAddress = formatAddress(incoming.endAddress || '');
            this.estimatedDistanceKm = incoming.distanceKm;
            this.estimatedDurationMin = incoming.estimatedTimeMinutes || incoming.estimatedDurationMin || incoming.estimatedDuration;
            this.rideId = incoming.id || incoming.rideId || undefined;

            // For admin rides without distanceKm, estimate from duration
            if (!this.estimatedDistanceKm && this.estimatedDurationMin) {
              this.estimatedDistanceKm = this.estimatedDurationMin * 0.8; // Rough estimate
            }

            if (incoming.route) {
              try { this.mapRouteService.drawRoute(incoming.route); } catch(e) {}
            } else if (incoming.routeLattitudes && incoming.routeLongitudes && Array.isArray(incoming.routeLattitudes) && Array.isArray(incoming.routeLongitudes) && incoming.routeLattitudes.length === incoming.routeLongitudes.length) {
              const pts = incoming.routeLattitudes.map((lat:any, i:number) => ({ lat: Number(lat), lng: Number(incoming.routeLongitudes[i]) }));
              try { this.mapRouteService.drawRoute(pts); } catch(e) {}
            }
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

    // fallback: fetch from backend (only for non-admin users)
    if (!this.isAdmin) {
      this.fetchCurrentRide();
    }
  }

 fetchCurrentRide(): void {
  this.rideService.getMyCurrentRide().subscribe({
    next: (response) => {
      this.destinationAddress = formatAddress(response.route.endLocation.address);
      this.pickupAddress = formatAddress(response.route.startLocation.address);
      this.estimatedDistanceKm = response.route.distanceKm;
      this.estimatedDurationMin = response.estimatedDurationMin;
      this.rideId = response.rideId;
      this.cdr.detectChanges();

      // Convert polylinePoints to route format for drawing
      const routePoints = response.route.polylinePoints.map(p => ({ lat: p.latitude, lng: p.longitude }));
      this.mapRouteService.drawRoute(routePoints);

      // Draw stop markers
      if (response.route.stopLocations && response.route.stopLocations.length > 0) {
        const stopPoints = response.route.stopLocations.map(s => ({
          lat: s.latitude,
          lng: s.longitude,
          name: formatAddress(s.address)
        }));
        this.mapRouteService.drawMarkers(stopPoints);
      }

      // Start tracking for passengers and admin users
      if (this.rideId && (this.isPassenger || this.isAdmin)) {
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
    this.destinationAddress = formatAddress(response.endAddress);
    this.finalPrice = response.price;

    if (response.route && Array.isArray(response.route)) {
      const routePoints = response.route.map((p: any) => ({ lat: p.lat, lng: p.lng }));
      this.mapRouteService.drawRoute(routePoints);
    }

    this.showMessageToast(`Ride stopped! Final price: ${this.finalPrice}`);
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

  openCompleteModal(): void {
    this.showCompleteModal = true;
  }

  onCompleteConfirmed(): void {
    this.showCompleteModal = false;
    this.markCompleted();
  }

  markCompleted(): void {
    if (!this.rideId) {
      this.showMessageToast('Ride ID not available.');
      return;
    }

    this.rideService.finishRide(this.rideId).subscribe({
      next: (hasNextRide) => {
        this.driverService.getMyStatus().subscribe();
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
        if (this.isPassenger || this.isAdmin) {
          this.mapRouteService.updateVehicleLocationAndCenter(
            trackData.location.latitude,
            trackData.location.longitude
          );
        } else {
          this.mapRouteService.updateVehicleLocation(
            trackData.location.latitude,
            trackData.location.longitude
          );
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error tracking ride:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.error);
        console.error('Full error:', JSON.stringify(err.error));
      }
    });

    // Then continue fetching every 10 seconds
    this.trackingSubscription = interval(10000).subscribe(() => {
      this.rideService.trackRide(rideId).subscribe({
        next: (trackData) => {
          this.remainingTimeMin = trackData.remainingTimeInMinutes;
          if (this.isPassenger || this.isAdmin) {
            this.mapRouteService.updateVehicleLocationAndCenter(
              trackData.location.latitude,
              trackData.location.longitude
            );
          } else {
            this.mapRouteService.updateVehicleLocation(
              trackData.location.latitude,
              trackData.location.longitude
            );
          }
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
    this.mapRouteService.clearRoute();
  }
}
