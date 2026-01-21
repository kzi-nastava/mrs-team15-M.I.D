import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RideService } from '../../../services/ride.service';

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

  constructor(private router: Router, private rideService: RideService) {}

  ngOnInit(): void {
    // If real data is passed via inputs (navigation state), call backend to create the order
    if (this.ride && !(this.ride as any).id) {
      this.state = 'searching';
      (async () => {
        try {
          const dto = { ...this.ride } as any;
          // call backend to create the order / find driver
          const res = await this.rideService.orderRide(dto);
          // merge server returned fields into local ride
          if (res) {
            try { Object.assign(this.ride, res); } catch (e) {}
          }
          // If backend returned assigned driver info, show found state
          if (res && res.assignedDriver) {
            this.foundDriver = {
              name: res.assignedDriver.name || 'Driver',
              etaMinutes: res.assignedDriver.etaMinutes || 0,
              vehicle: res.assignedDriver.vehicle || '',
              plate: res.assignedDriver.plate || '',
              photo: res.assignedDriver.photo || '/assets/pfp/default-avatar-icon.jpg'
            };
            this.state = 'found';
          } else {
            // remain in searching; parent or polling can update later
            this.state = 'searching';
          }
        } catch (e) {
          console.error('Order/create call in finding-driver failed', e);
          this.state = 'notfound';
        }
      })();
    }
  }

  // UI state: 'searching' | 'found' | 'notfound'
  state: 'searching' | 'found' | 'notfound' = 'searching';

  // mock driver info when a driver is found
  foundDriver: DriverInfo | null = null;

  // Simulation helpers (dev buttons)
  simulateFound() {
    this.foundDriver = {
      name: 'Milan Petrović',
      etaMinutes: 4,
      vehicle: 'Škoda Octavia (black)',
      plate: 'BG-123-AB',
      photo: '/assets/pfp/default-avatar-icon.jpg',
    };
    this.state = 'found';
  }

  simulateNotFound() {
    this.foundDriver = null;
    this.state = 'notfound';
  }

  resetSearch() {
    this.foundDriver = null;
    this.state = 'searching';
  }

  accept() {
    this.router.navigate(['/current-ride']);
  }

  backToRideDetail() {
    const id = (this.ride as any).id;
    if (id !== undefined && id !== null) {
      this.router.navigate(['/ride-details', id]);
    } else {
      // fallback: go back to previous page or home
      if (window && window.history && window.history.length > 1) {
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
