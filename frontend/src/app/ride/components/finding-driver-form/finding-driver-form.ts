import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RideService } from '../../../services/ride.service';
import { AdminService } from '../../../services/admin.service';
import { lastValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';

// Interface for ride information
interface RideInfo {
  startAddress: string;
  endAddress: string;
  id?: string | number;
  distanceKm?: number;
  price?: number;
  passengers?: number;
  vehicleType?: string;
}

// Interface for driver information
interface DriverInfo {
  name: string;
  etaMinutes: number;
  vehicle: string;
  plate?: string;
  photo?: string;
}

// Component that handles driver search and assignment
// Shows searching state, then either found driver or not found
@Component({
  selector: 'app-finding-driver-form',
  imports: [CommonModule],
  templateUrl: './finding-driver-form.html',
  styleUrl: './finding-driver-form.css',
})
export class FindingDriverForm implements OnInit {
  // Ride information passed from ordering form
  @Input() ride: RideInfo = {
    startAddress: 'Bulevar cara Lazara 80',
    endAddress: 'Nemanjina 4',
    distanceKm: 30,
    price: 860,
    passengers: 1,
    vehicleType: 'Standard',
  };

  constructor(
    private router: Router,
    private rideService: RideService,
    private adminService: AdminService,
    private cd: ChangeDetectorRef
  ) {}

  // On init, orders ride and searches for driver
  ngOnInit(): void {
    // If ride doesn't have an id, it's a new order
    if (this.ride && !(this.ride as any).id) {
      this.state = 'searching';
      (async () => {
        try {
          const dto = { ...this.ride } as any;
          
          const searchStart = Date.now();
          
          const res = await this.rideService.orderRide(dto);
          
          const backendDriverId = (res && ('driverId' in res) && res.driverId !== undefined)
            ? res.driverId
            : (res && res.assignedDriver && ('driverId' in res.assignedDriver) ? res.assignedDriver.driverId : undefined);
          try {
            console.log('order response:', res);
            console.log('normalized driverId:', backendDriverId);
          } catch (e) {}
          // ensure UI stays on 'searching' for at least 3 seconds
          try { await this.ensureMinSearchDuration(searchStart, 3000); } catch (e) {}
          
          if (res) {
            try { Object.assign(this.ride, res); } catch (e) {}
          }
          
          if (backendDriverId == null || backendDriverId === '') {
            try { console.log('Decision: no driverId -> notfound'); } catch (e) {}
            this.foundDriver = null;
            this.enterNotFoundState();
          } else {
            
            try { console.log('Decision: driverId present -> found'); } catch (e) {}
            
            let driverUser: any = null;
            const driverIdNum = Number(backendDriverId);
            if (!isNaN(driverIdNum)) {
              try {
                driverUser = await lastValueFrom(this.adminService.getUserById(driverIdNum));
                try { console.log('driver user:', driverUser); } catch (e) {}
              } catch (e) {
                console.warn('Failed to fetch driver user by id', driverIdNum, e);
              }
            }

            if (driverUser) {
              const name = (driverUser.firstName || driverUser.name || (driverUser.lastName ? `${driverUser.firstName || ''} ${driverUser.lastName || ''}`.trim() : 'Driver')) || 'Driver';
              const photo =  environment.backendUrl + (driverUser.profileImage ||  '/uploads/default.jpg');
              const vehicle = res?.assignedDriver?.vehicleModel || driverUser.vehicleModel || '';
              const plate = res?.assignedDriver?.licensePlate || driverUser.licensePlate || '';
              const eta = res?.assignedDriver?.ETA || res?.estimatedTimeMinutes || 0;
              this.foundDriver = {
                name,
                etaMinutes: eta,
                vehicle,
                plate,
                photo,
              };
            } else if (res && res.assignedDriver) {
              this.foundDriver = {
                name: res.assignedDriver.name || 'Driver',
                etaMinutes: res.assignedDriver.etaMinutes || 0,
                vehicle: res.assignedDriver.vehicleModel || '',
                plate: res.assignedDriver.licensePlate || '',
                photo: environment.backendUrl + (res.assignedDriver.photo || '/uploads/default.jpg')
              };
            } 
            this.enterFoundState();
          }
        } catch (e) {
          console.error('Order/create call in finding-driver failed', e);
          this.enterNotFoundState();
        }
      })();
    }
  }

  // Current UI state (searching/found/notfound)
  state: 'searching' | 'found' | 'notfound' = 'searching';

  // Driver info when found
  foundDriver: DriverInfo | null = null;

  // Lock flag to prevent state changes during animation
  foundLocked = false;
  private _foundLockTimer: any = null;

  // Enters found state and locks for 3 seconds
  private enterFoundState() {
    this.foundLocked = true;
    this.state = 'found';
    try { this.cd.detectChanges(); } catch (e) {}
    try { clearTimeout(this._foundLockTimer); } catch (e) {}
    this._foundLockTimer = setTimeout(() => {
      this.foundLocked = false;
      try { this.cd.detectChanges(); } catch (e) {}
    }, 3000);
  }

  // Lock flag for not found state
  notFoundLocked = false;
  private _notFoundLockTimer: any = null;

  // Enters not found state and locks for 3 seconds
  private enterNotFoundState() {
    this.notFoundLocked = true;
    this.state = 'notfound';
    try { this.cd.detectChanges(); } catch (e) {}
    try { clearTimeout(this._notFoundLockTimer); } catch (e) {}
    this._notFoundLockTimer = setTimeout(() => {
      this.notFoundLocked = false;
      try { this.cd.detectChanges(); } catch (e) {}
    }, 3000);
  }

  // Ensures minimum search duration for better UX
  private async ensureMinSearchDuration(startMs: number, minMs: number) {
    const elapsed = Date.now() - startMs;
    if (elapsed < minMs) {
      await new Promise((resolve) => setTimeout(resolve, minMs - elapsed));
    }
  }

  // Resets to searching state
  resetSearch() {
    this.foundDriver = null;
    this.state = 'searching';
  }

  // Accepts ride and navigates to upcoming rides
  accept() {
    this.router.navigate(['/upcoming-rides']);
  }

  // Navigates back to ride detail page
  backToRideDetail() {
    // Navigate to the current-ride page and pass the ride info via navigation state
    try {
      this.router.navigate(['/upcoming-rides'], { state: { ride: this.ride } });
    } catch (e) {
      // fallback to previous behavior
      const id = (this.ride as any).id;
      if (id !== undefined && id !== null) {
        this.router.navigate(['/upcoming-rides', id]);
      } else if (window && window.history && window.history.length > 1) {
        window.history.back();
      } else {
        this.router.navigate(['/home']);
      }
    }
  }

  // Cancels ride search and returns to home
  onCancel() {
    this.router.navigate(['/home']);
  }

  // Navigates to ride ordering page
  goToOrdering() {
    this.router.navigate(['/ride-ordering']);
  }

  // Formats distance for display
  formatDistance(): string {
    return (this.ride.distanceKm ?? 0) + ' km';
  }

  // Formats price for display
  formatPrice(): string {
    const p = this.ride.price ?? 0;
    return p.toFixed(2) + ' RSD';
  }

  // Splits address into primary and secondary lines based on commas
  getAddressLines(address: string): { primary: string; secondary: string } {
    if (!address) return { primary: '', secondary: '' };
    const raw = String(address).trim();
    
    const commaPositions: number[] = [];
    for (let i = 0; i < raw.length; i++) if (raw[i] === ',') commaPositions.push(i);
    if (commaPositions.length >= 3) {
      
      const thirdCommaIdx = commaPositions[2];
      const primary = raw.slice(0, thirdCommaIdx).trim(); 
      
      const secondary = raw.slice(thirdCommaIdx + 1).trim();
      return { primary, secondary };
    }
    if (commaPositions.length >= 2) {
      const secondCommaIdx = commaPositions[1];
      const primary = raw.slice(0, secondCommaIdx + 1).trim(); 
      
      const secondary = raw.slice(secondCommaIdx + 1).trim();
      return { primary, secondary };
    }
    if (commaPositions.length === 1) {
      const firstCommaIdx = commaPositions[0];
      const primary = raw.slice(0, firstCommaIdx + 1).trim(); 
      
      const secondary = raw.slice(firstCommaIdx + 1).trim();
      return { primary, secondary };
    }
    
    return { primary: raw, secondary: '' };
  }
}
