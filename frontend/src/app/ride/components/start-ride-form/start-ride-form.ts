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
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-start-ride-form',
  standalone: true,
  imports: [Button, CommonModule, MissingPassengersModal],
  templateUrl: './start-ride-form.html',
  styleUrls: ['./start-ride-form.css'],
})

export class StartRideForm implements OnInit, OnChanges {
  @Input() ride?: UpcomingRide | null;
  pickupLocation: string = 'Bulevar Mihajla Pupina 10, Novi Sad';
  destination: string = 'Futoška 10, Novi Sad';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private driverService: DriverService,
    private rideService: RideService,
    private cdr: ChangeDetectorRef,
    private mapRouteService: MapRouteService,
  ) {}

  // backend base url used to prefix relative image paths returned by API
  backendUrl: string = environment.backendUrl;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ride'] && changes['ride'].currentValue) {
      this.triggerPassengerPickup();
    }
  }

  private triggerPassengerPickup(): void {
    const idParam = this.route.snapshot.queryParams['id'];
    let rideId = this.ride && this.ride.id ? this.ride.id : (idParam ? +idParam : NaN);
   
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

    const passengerKey = 'passengers';
      if (res[passengerKey] && Array.isArray(res[passengerKey])) {
        this.passengers = res[passengerKey].map((p: any) => {
          let name = '';
          if (typeof p === 'string') name = p;
          else name = p.name ?? p.fullName ?? (p.firstName && p.lastName ? `${p.firstName} ${p.lastName}` : JSON.stringify(p));
          return { name: name, present: false, image: null } as any;
        });
        this.presentCount = this.passengers.filter(p => p.present).length;
        
      }
    

    // attach passenger images if backend returned them (by index)
    const imageKey = 'passengerImages';
      if (res[imageKey] && Array.isArray(res[imageKey]) && this.passengers && this.passengers.length > 0) {
        const imgs = res[imageKey];
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
    
      }
    

    // pickup location
    const pickup = 'startAddress';
    if (res[pickup]) { this.pickupLocation = String(res[pickup]); }

    // destination
    const dest =  'endAddress';
    if (res[dest]) { this.destination = String(res[dest]); }
    

    const routeKey = 'route';
      if (res[routeKey]) {
        let raw = res[routeKey];
        let normalized: any[] = [];
        try {
          if (typeof raw === 'string') {
            // JSON parse
            try { raw = JSON.parse(raw); } catch (e) { }
          }

          if (Array.isArray(raw)) {
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
          
          try {
            const pts = [];
            if (normalized.length > 0) pts.push(normalized[0]);
            if (res['stopLats'] && res['stopLngs'] && Array.isArray(res['stopLats']) && Array.isArray(res['stopLngs']) && res['stopLats'].length === res['stopLngs'].length) {
              for (let i = 0; i < res['stopLats'].length; i++) {
                const slat = Number(res['stopLats'][i]);
                const slng = Number(res['stopLngs'][i]);
                if (Number.isFinite(slat) && Number.isFinite(slng)) {
                  pts.push({ lat: slat, lng: slng });
                }
            }
            }
            if (normalized.length > 1) pts.push(normalized[normalized.length - 1]);
            
            if (pts.length > 0) this.mapRouteService.drawMarkers(pts, !!res.isAlert);
          } catch (e) {}
          
        }
      }
    
  }

  @ViewChild('missingModal') missingModal!: MissingPassengersModal;
  confirmMessage: string = '';
  missingPassengers: string[] = [];
  canStart: boolean = false;
  presentCount: number = 0;

  startRide(): void {
    // finding missing passengers
    this.missingPassengers = this.passengers.filter(p => !p.present).map(p => p.name);
    this.presentCount = this.passengers.filter(p => p.present).length;
    this.canStart = this.presentCount > 0;

    if (this.presentCount === 0) {
      this.confirmMessage = 'No passengers are present. You cannot start the ride.';
    } else if (this.missingPassengers.length > 0) {
      this.confirmMessage = 'The following passengers are not present:';
    } else {
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

    if (this.missingModal) {
      this.missingModal.confirmMessage = this.confirmMessage;
      this.missingModal.missingPassengers = this.missingPassengers;
      this.missingModal.canStart = this.canStart;
      this.missingModal.openModal();
    }
  }

  closeConfirmModal(): void {
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
  
  // Mock passenger data
  passengers: { name: string; present: boolean; image?: string | null }[] = [
    { name: 'Marko Marković', present: false, image: null },
    { name: 'Ana Jovanović', present: false, image: null },
  ];

  trackByPassenger(index: number, passenger: { name: string; present: boolean }) {
    return passenger.name;
  }

  togglePassenger(passenger: { name: string; present: boolean }) {
    passenger.present = !passenger.present;
    this.presentCount = this.passengers.filter(p => p.present).length;
  }

  ngOnInit(): void {
    this.triggerPassengerPickup();
    if (this.ride && this.ride.passengers) {
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
