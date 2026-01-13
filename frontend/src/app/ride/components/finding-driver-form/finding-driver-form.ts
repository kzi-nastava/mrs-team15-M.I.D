import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

interface RideInfo {
  from: string;
  to: string;
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
    from: 'Bulevar cara Lazara 80',
    to: 'Nemanjina 4',
    distanceKm: 30,
    price: 860,
    passengers: 1,
    vehicleType: 'Standard',
  };

  constructor(private router: Router) {}

  ngOnInit(): void {
    // If real data will be passed via inputs, it will be shown automatically.
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
}
