import { Component, ViewChild, Input, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { DriverService } from '../../../services/driver.service';
import { RideService } from '../../../services/ride.service';
import { MapRouteService } from '../../../services/map-route.service';
import { MissingPassengersModal } from '../missing-passengers-modal/missing-passengers-modal';
import { UpcomingRide } from '../upcoming-rides-table/upcoming-rides-table';

@Component({
  selector: 'app-start-ride-form',
  imports: [Button, CommonModule, MissingPassengersModal],
  templateUrl: './start-ride-form.html',
  styleUrl: './start-ride-form.css',
})

export class StartRideForm implements OnInit, OnChanges {
  @Input() ride?: UpcomingRide | null;
  // displayable ride fields (defaults kept for existing UI)
  pickupLocation: string = 'Bulevar Mihajla Pupina 10, Novi Sad';
  destination: string = 'Futoška 10, Novi Sad';
  estimatedArrival: string = '4 min';
  backendUrl: string = 'http://localhost:8081';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private driverService: DriverService,
    private rideService: RideService,
    private cdr: ChangeDetectorRef,
    private mapRouteService: MapRouteService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ride'] && changes['ride'].currentValue) {
      this.triggerPassengerPickup();
    }
  }

  private triggerPassengerPickup(): void {
    const idParam = this.route.snapshot.queryParams['id'];
    let rideId = this.ride && this.ride.id ? this.ride.id : (idParam ? +idParam : NaN);
    // fallback: try navigation extras or window.history.state
    if (!Number.isFinite(rideId)) {
      const nav = this.router.getCurrentNavigation?.();
      const navRideId = nav?.extras?.state?.['ride']?.id ?? null;
      const histRideId = (window && (window.history && (window.history.state && window.history.state['ride']))) ? window.history.state['ride'].id : null;
      if (navRideId) rideId = +navRideId;
      else if (histRideId) rideId = +histRideId;
    }

    if (!Number.isFinite(rideId)) {
      rideId = 70;
    }

    this.rideService.passengerPickup(rideId).subscribe({
      next: (res) => {
        console.log('passengerPickup response:', res);
        this.applyBackendDataToUI(res);
        try { this.cdr.detectChanges(); } catch (e) { /* ignore */ }
      },
      error: (err) => {
        console.error('passengerPickup error:', err);
        try { this.cdr.detectChanges(); } catch (e) { /* ignore */ }
      }
    });
  }

  private applyBackendDataToUI(res: any) {
    if (!res) return;

    // passengers: accept array of strings or objects
    const passengerKeys = ['passengers', 'passengerList', 'passengerDtos', 'passengerDTOs'];
    for (const k of passengerKeys) {
      if (res[k] && Array.isArray(res[k])) {
        this.passengers = res[k].map((p: any) => {
          let name = '';
          if (typeof p === 'string') name = p;
          else name = p.name ?? p.fullName ?? (p.firstName && p.lastName ? `${p.firstName} ${p.lastName}` : JSON.stringify(p));
          return { name: name, present: false, image: null } as any;
        });
        this.presentCount = this.passengers.filter(p => p.present).length;
        break;
      }
    }

    // attach passenger images if backend returned them (by index)
    const imageKeys = ['passengerImages', 'passengerImageUrls', 'passengerPhotos', 'passengerPhotoUrls'];
    for (const k of imageKeys) {
      if (res[k] && Array.isArray(res[k]) && this.passengers && this.passengers.length > 0) {
        const imgs = res[k];
        for (let i = 0; i < imgs.length && i < this.passengers.length; i++) {
          const raw = imgs[i];
          if (!raw) continue;
          let url = '';
          if (typeof raw === 'string') url = raw;
          else if (raw.url) url = raw.url;
          else if (raw.path) url = raw.path;

          // prefix backend url when path is relative
          if (url && url.startsWith('/')) url = this.backendUrl + url;
          this.passengers[i].image = url || null;
        }
        break;
      }
    }

    // pickup location
    const pickupCandidates = ['pickupLocation', 'pickup', 'startAddress', 'startLocation'];
    for (const k of pickupCandidates) {
      if (res[k]) { this.pickupLocation = String(res[k]); break; }
    }

    // destination
    const destCandidates = ['destination', 'destinationAddress', 'endAddress', 'dropoff'];
    for (const k of destCandidates) {
      if (res[k]) { this.destination = String(res[k]); break; }
    }

    // estimated arrival
    if (res.estimatedArrival) this.estimatedArrival = String(res.estimatedArrival);
    else if (res.estimatedArrivalMinutes) this.estimatedArrival = res.estimatedArrivalMinutes + ' min';
    else if (res.etaMinutes) this.estimatedArrival = res.etaMinutes + ' min';

    // Route data: normalize and draw on map via MapRouteService
    const routeKeys = ['route', 'path', 'points', 'polyline', 'coords', 'coordinates'];
    for (const k of routeKeys) {
      if (res[k]) {
        let raw = res[k];
        let normalized: any[] = [];
        try {
          // If polyline string (encoded), try to decode if helper exists
          if (typeof raw === 'string') {
            // try JSON parse
            try { raw = JSON.parse(raw); } catch (e) { /* leave as string */ }
          }

          if (Array.isArray(raw)) {
            // array of [lat,lng] or objects
            if (raw.length > 0 && Array.isArray(raw[0]) && raw[0].length >= 2) {
              normalized = raw.map((p: any) => ({ lat: Number(p[0]), lng: Number(p[1]) }));
            } else {
              normalized = raw.map((p: any) => {
                if (typeof p === 'string') {
                  const split = p.split(',').map(s => s.trim());
                  return { lat: Number(split[0]), lng: Number(split[1]) };
                }
                return { lat: Number(p.lat ?? p.latitude ?? p.latLng?.lat ?? p[0]), lng: Number(p.lng ?? p.longitude ?? p.latLng?.lng ?? p[1]) };
              });
            }
          }
        } catch (e) { normalized = []; }

        if (normalized && normalized.length > 0) {
          try { this.mapRouteService.drawRoute(normalized, !!res.isAlert); } catch(e) { /* ignore */ }
          // draw only start and end markers
          try {
            const pts = [];
            if (normalized.length > 0) pts.push(normalized[0]);
            if (normalized.length > 1) pts.push(normalized[normalized.length - 1]);
            if (pts.length > 0) this.mapRouteService.drawMarkers(pts, !!res.isAlert);
          } catch (e) { /* ignore */ }
          break;
        }
      }
    }
  }

  // Modal handling via MissingPassengersModal
  @ViewChild('missingModal') missingModal!: MissingPassengersModal;
  confirmMessage: string = '';
  missingPassengers: string[] = [];
  canStart: boolean = false;
  presentCount: number = 0;

  startRide(): void {
    // compute missing passengers
    this.missingPassengers = this.passengers.filter(p => !p.present).map(p => p.name);
    this.presentCount = this.passengers.filter(p => p.present).length;
    this.canStart = this.presentCount > 0;

    if (this.presentCount === 0) {
      this.confirmMessage = 'No passengers are present. You cannot start the ride.';
      // show modal to inform driver nothing is present
    } else if (this.missingPassengers.length > 0) {
      this.confirmMessage = 'The following passengers are not present:';
      // show modal listing missing passengers and allow confirm
    } else {
      // all passengers present -> attempt to call backend to start ride
      const rideId = this.getRideId();
      if (Number.isFinite(rideId)) {
        this.driverService.startRide(rideId).subscribe({
          next: (res) => { console.log('startRide response:', res); this.router.navigate(['/current-ride']); },
          error: (err) => { console.error('startRide error:', err); this.router.navigate(['/current-ride']); }
        });
      } else {
        // no ride id provided — fall back to previous local navigation
        this.router.navigate(['/current-ride']);
      }
      return;
    }

    // open the reusable modal component for the other cases
    if (this.missingModal) {
      this.missingModal.confirmMessage = this.confirmMessage;
      this.missingModal.missingPassengers = this.missingPassengers;
      this.missingModal.canStart = this.canStart;
      this.missingModal.openModal();
    }
  }

  closeConfirmModal(): void {
    // delegate to modal component
    if (this.missingModal) {
      this.missingModal.closeModal();
    }
  }

  confirmStart(): void {
    if (!this.canStart) {
      // do nothing if not allowed to start
      return;
    }
    // Proceed to start ride
    this.closeConfirmModal();
    const rideId = this.getRideId();
    if (Number.isFinite(rideId)) {
      this.driverService.startRide(rideId).subscribe({
        next: (res) => { console.log('confirmStart startRide response:', res); this.router.navigate(['/current-ride']); },
        error: (err) => { console.error('confirmStart startRide error:', err); this.router.navigate(['/current-ride']); }
      });
    } else {
      this.router.navigate(['/current-ride']);
    }
  }
  
  // Example passengers list — replace with real data as needed
  passengers: { name: string; present: boolean; image?: string | null }[] = [
    { name: 'Marko Marković', present: false, image: null },
    { name: 'Ana Jovanović', present: false, image: null },
  ];

  trackByPassenger(index: number, passenger: { name: string; present: boolean }) {
    return passenger.name;
  }

  togglePassenger(passenger: { name: string; present: boolean }) {
    passenger.present = !passenger.present;
    // update present count reactively so template reflects state immediately
    this.presentCount = this.passengers.filter(p => p.present).length;
  }

  ngOnInit(): void {
    // Always attempt to trigger passenger pickup when component initializes.
    // triggerPassengerPickup will try multiple fallbacks to obtain the ride id.
    this.triggerPassengerPickup();

    // If ride data is provided via navigation state, initialize passengers list
    if (this.ride && this.ride.passengers) {
      // assume `ride.passengers` is a comma-separated string
      this.passengers = this.ride.passengers.split(',').map((n: string) => ({ name: n.trim(), present: false }));
      this.presentCount = this.passengers.filter(p => p.present).length;
    }
  }

  private getRideId(): number {
    const idParam = this.route.snapshot.queryParams['id'];
    let rideId = this.ride && (this.ride as any).id ? (this.ride as any).id : (idParam ? +idParam : NaN);
    if (!Number.isFinite(rideId)) {
      const nav = this.router.getCurrentNavigation?.();
      const navRideId = nav?.extras?.state?.['ride']?.id ?? null;
      const histRideId = (window && (window.history && (window.history.state && window.history.state['ride']))) ? window.history.state['ride'].id : null;
      if (navRideId) rideId = +navRideId;
      else if (histRideId) rideId = +histRideId;
    }
    if (!Number.isFinite(rideId)) rideId = 70;
    return rideId;
  }
}
