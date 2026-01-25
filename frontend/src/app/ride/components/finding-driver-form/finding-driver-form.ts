import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RideService } from '../../../services/ride.service';
import { AdminService } from '../../../services/admin.service';
import { lastValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';

interface RideInfo {
  startAddress: string;
  endAddress: string;
  id?: string | number;
  distanceKm?: number;
  price?: number;
  passengers?: number;
  vehicleType?: string;
}

interface DriverInfo {
  name: string;
  etaMinutes: number;
  vehicle: string;
  plate?: string;
  photo?: string;
}

@Component({
  selector: 'app-finding-driver-form',
  imports: [CommonModule],
  templateUrl: './finding-driver-form.html',
  styleUrl: './finding-driver-form.css',
})
export class FindingDriverForm implements OnInit {
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

  ngOnInit(): void {
    // If real data is passed via inputs (navigation state), call backend to create the order
    if (this.ride && !(this.ride as any).id) {
      this.state = 'searching';
      (async () => {
        try {
          const dto = { ...this.ride } as any;
          // record when searching started so we can enforce a minimum 'searching' duration
          const searchStart = Date.now();
          // call backend to create the order / find driver
          const res = await this.rideService.orderRide(dto);
          // normalize and log driverId returned by backend for debugging/visibility
          const backendDriverId = (res && ('driverId' in res) && res.driverId !== undefined)
            ? res.driverId
            : (res && res.assignedDriver && ('driverId' in res.assignedDriver) ? res.assignedDriver.driverId : undefined);
          try {
            console.log('order response:', res);
            console.log('normalized driverId:', backendDriverId);
          } catch (e) {}
          // ensure UI stays on 'searching' for at least 3 seconds
          try { await this.ensureMinSearchDuration(searchStart, 3000); } catch (e) {}
          // merge server returned fields into local ride
          if (res) {
            try { Object.assign(this.ride, res); } catch (e) {}
          }
          // If backend explicitly indicates no available drivers via driverId (null/empty), show notfound
          if (backendDriverId == null || backendDriverId === '') {
            try { console.log('Decision: no driverId -> notfound'); } catch (e) {}
            this.foundDriver = null;
            this.enterNotFoundState();
          } else {
            // backend indicates a driver was assigned (driverId present)
            try { console.log('Decision: driverId present -> found'); } catch (e) {}
            // Try to fetch full user details for the driver from AdminService
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
              const photo =  environment.backendUrl + (driverUser.profileImage ||  '/assets/pfp/default-avatar-icon.jpg');
              const vehicle = res?.assignedDriver?.vehicleModel || driverUser.vehicleModel || '';
              const plate = res?.assignedDriver?.licensePlate || driverUser.licensePlate || '';
              const eta = res?.assignedDriver?.etaMinutes || res?.estimatedTimeMinutes || 0;
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
                photo: environment.backendUrl + (res.assignedDriver.photo || '/assets/pfp/default-avatar-icon.jpg')
              };
            } else {
              // assignedDriver details missing: create a minimal driver info using available fields
              this.foundDriver = {
                name: 'Driver',
                etaMinutes: (res && (res.estimatedTimeMinutes || res.etaMinutes)) || 0,
                vehicle: (res && res.vehicleModel) || '',
                plate: '',
                photo: '/assets/pfp/default-avatar-icon.jpg'
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

  // UI state: 'searching' | 'found' | 'notfound'
  state: 'searching' | 'found' | 'notfound' = 'searching';

  // mock driver info when a driver is found
  foundDriver: DriverInfo | null = null;

  // When true, UI stays on found state and interaction is locked for a short period
  foundLocked = false;
  private _foundLockTimer: any = null;

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

  // not-found lock
  notFoundLocked = false;
  private _notFoundLockTimer: any = null;

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

  private async ensureMinSearchDuration(startMs: number, minMs: number) {
    const elapsed = Date.now() - startMs;
    if (elapsed < minMs) {
      await new Promise((resolve) => setTimeout(resolve, minMs - elapsed));
    }
  }

  // Simulation helpers (dev buttons)
  simulateFound() {
    this.foundDriver = {
      name: 'Milan Petrović',
      etaMinutes: 4,
      vehicle: 'Škoda Octavia (black)',
      plate: 'BG-123-AB',
      photo: '/assets/pfp/default-avatar-icon.jpg',
    };
    this.enterFoundState();
  }

  simulateNotFound() {
    this.foundDriver = null;
    this.enterNotFoundState();
  }

  resetSearch() {
    this.foundDriver = null;
    this.state = 'searching';
  }

  accept() {
    this.router.navigate(['/current-ride']);
  }

  backToRideDetail() {
    // Navigate to the current-ride page and pass the ride info via navigation state
    try {
      this.router.navigate(['/current-ride'], { state: { ride: this.ride } });
    } catch (e) {
      // fallback to previous behavior
      const id = (this.ride as any).id;
      if (id !== undefined && id !== null) {
        this.router.navigate(['/ride-details', id]);
      } else if (window && window.history && window.history.length > 1) {
        window.history.back();
      } else {
        this.router.navigate(['/home']);
      }
    }
  }

  onCancel() {
    this.router.navigate(['/home']);
  }

  goToOrdering() {
    this.router.navigate(['/ride-ordering']);
  }

  formatDistance(): string {
    return (this.ride.distanceKm ?? 0) + ' km';
  }

  formatPrice(): string {
    const p = this.ride.price ?? 0;
    return p.toFixed(2) + ' RSD';
  }

  // Split an address into primary and secondary parts for nicer display
  getAddressLines(address: string): { primary: string; secondary: string } {
    if (!address) return { primary: '', secondary: '' };
    const raw = String(address).trim();
    // Find comma positions to include commas in the primary part
    const commaPositions: number[] = [];
    for (let i = 0; i < raw.length; i++) if (raw[i] === ',') commaPositions.push(i);
    if (commaPositions.length >= 3) {
      // include text up to (but not including) the third comma
      const thirdCommaIdx = commaPositions[2];
      const primary = raw.slice(0, thirdCommaIdx).trim(); // exclude third comma
      const secondary = raw.slice(thirdCommaIdx + 1).trim();
      return { primary, secondary };
    }
    if (commaPositions.length >= 2) {
      const secondCommaIdx = commaPositions[1];
      const primary = raw.slice(0, secondCommaIdx + 1).trim(); // include second comma
      const secondary = raw.slice(secondCommaIdx + 1).trim();
      return { primary, secondary };
    }
    if (commaPositions.length === 1) {
      const firstCommaIdx = commaPositions[0];
      const primary = raw.slice(0, firstCommaIdx + 1).trim(); // include first comma
      const secondary = raw.slice(firstCommaIdx + 1).trim();
      return { primary, secondary };
    }
    // no commas: try to break roughly in the middle at a space
    if (raw.length <= 30) return { primary: raw, secondary: '' };
    const breakPos = Math.max(20, Math.min(40, Math.floor(raw.length / 2)));
    let idx = raw.indexOf(' ', breakPos);
    if (idx === -1) idx = raw.indexOf(' ', Math.floor(raw.length / 3));
    if (idx === -1) return { primary: raw, secondary: '' };
    return { primary: raw.slice(0, idx).trim(), secondary: raw.slice(idx + 1).trim() };
  }
}
