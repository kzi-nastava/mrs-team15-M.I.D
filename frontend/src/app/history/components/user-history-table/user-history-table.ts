import { Component, Input, ViewChild, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { PassengerService } from '../../../services/passenger.service';
import { CommonModule } from '@angular/common';
import { AddFavoriteModal } from '../add-favorite-modal/add-favorite-modal';
import { RemoveFavoriteModal } from '../remove-favorite-modal/remove-favorite-modal';
import { Button } from '../../../shared/components/button/button';

export interface Ride{
  id: number;
  route: string;
  
  routeId?: number | null;
  startTime: string;
  endTime: string;
  passengers: string;
  driver: string;
  cancelled: string | null;
  cancelledBy: string | null;
  cost: string;
  panicButton: string | null;
  panicBy: string | null;
  rating?: number | null;
  inconsistencies?: string[] | null;
  favorite?: boolean;
  pickupAddress?: string | null;
  destinationAddress?: string | null;
  stopAddress?: string | null;
  stopAddresses?: string[] | null;
}

type SortColumn = 'route' | 'startTime' | 'endTime' ;
type SortDirection = 'asc' | 'desc' | '';

@Component({
  selector: 'app-user-history-table',
  standalone: true,
  imports: [CommonModule, AddFavoriteModal, RemoveFavoriteModal, Button],
  templateUrl: './user-history-table.html',
  styleUrl: './user-history-table.css',
})
export class UserHistoryTable {
  private _rides : Ride[] = []

  @ViewChild('addFav') addFavModal!: AddFavoriteModal;
  @ViewChild('removeFav') removeFavModal!: RemoveFavoriteModal;

  
  private pendingRide: Ride | null = null;

  constructor(private router: Router, private cdr: ChangeDetectorRef, private passengerService: PassengerService) {}

  @Input()
  set rides(value: Ride[]) {
    this._rides = value || [];
    if (this._rides.length > 0) {
      this.applySorting();
    }
    try { this.cdr.detectChanges(); } catch (e) {}
  }
  get rides(): Ride[] {
    return this._rides;
  }

  sortColumn: SortColumn | '' = 'startTime';
  sortDirection: SortDirection = 'asc';

  ngOnInit(): void {
    if (this._rides.length > 0) {
      this.applySorting();
    }
  }

  sort(column: SortColumn): void {
    if (this.sortColumn === column) {
      if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc';
      } else if (this.sortDirection === 'desc') {
        this.sortDirection = '';
        this.sortColumn = '';
        return;
      }
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applySorting();
  }

private applySorting(): void {
  if (!this.sortColumn || !this.sortDirection) return;

  this._rides.sort((a, b) => {
    let aValue: any = a[this.sortColumn as SortColumn];
    let bValue: any = b[this.sortColumn as SortColumn];

    if (this.sortColumn === 'startTime' || this.sortColumn == 'endTime') {
      const parse = (value: string) => {
        const [datePart, timePart] = value.split(', ');
        const [day, month, year] = datePart.split('-').map(Number);
        const [hour, minute] = timePart.split(':').map(Number);
        return new Date(year, month - 1, day, hour, minute).getTime();
      };

      aValue = parse(a.startTime);
      bValue = parse(b.startTime);
    }

    else {
      aValue = aValue.toLowerCase();
      bValue = bValue.toLowerCase();
    }

    if (aValue < bValue) {
      return this.sortDirection === 'asc' ? -1 : 1;
    }
    if (aValue > bValue) {
      return this.sortDirection === 'asc' ? 1 : -1;
    }
    return 0;
  });
}

  getSortIcon(column: SortColumn): string {
    if (this.sortColumn !== column) {
      return '⇅';
    }
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  viewRideDetails(ride: Ride): void {
    this.router.navigate(['/history-ride-details', ride.id], { state: { ride } });
  }

  rateRide(ride: Ride): void {
    this.router.navigate(['/rating', ride.id]);
  }

  canRate(ride: Ride): boolean {
    if (!ride.endTime || ride.rating) return false;

    
    try {
      const [datePart, timePart] = ride.endTime.split(', ');
      const [day, month, year] = datePart.split('-').map(Number);
      const [hour, minute] = timePart.split(':').map(Number);

      const rideDate = new Date(year, month - 1, day, hour, minute);
      const now = new Date();
      const threeDaysAgo = new Date(now.getTime() - (3 * 24 * 60 * 60 * 1000));

      return rideDate >= threeDaysAgo;
    } catch (e) {
      return false;
    }
  }

  // Request toggle: open appropriate modal
  requestToggleFavorite(ride: Ride): void {
    this.pendingRide = ride;
    const label = ride.route || `Ride #${ride.id}`;
    const stops = ride.stopAddresses ?? (ride.stopAddress ? [ride.stopAddress] : []);
    let info = { pickup: ride.pickupAddress ?? null, destination: ride.destinationAddress ?? null, stop: stops };

    
    const noPickup = !info.pickup;
    const noDestination = !info.destination;
    const noStops = !info.stop || (Array.isArray(info.stop) && info.stop.length === 0);
    if (noPickup && noDestination && noStops) {
      info = {
        pickup: 'Main Street 123, City',
        destination: 'Central Station, City',
        stop: ['Park Ave 5', 'Mall Entrance']
      };
    }

    if (ride.favorite) {
      if (this.removeFavModal) this.removeFavModal.openModal(label, info);
    } else {
      if (this.addFavModal) this.addFavModal.openModal(label, info);
    }
  }

  // Called when add-favorite modal confirm is emitted
  onAddFavoriteConfirmed(): void {
    if (!this.pendingRide) return;
    const routeId = (this.pendingRide as any).routeId;
    if (routeId) {
      this.passengerService.addFavorite(routeId).subscribe({
        next: () => {
          // mark all rides that share this routeId as favorite
          try {
            this._rides.forEach(r => { if (r.routeId === routeId) r.favorite = true; });
          } catch (e) {}
          this.pendingRide = null;
          try { this.cdr.detectChanges(); } catch (e) {}
        },
        error: (e) => {
          console.warn('Failed to add favorite', e);
          this.pendingRide = null;
          try { this.cdr.detectChanges(); } catch (e) {}
        }
      });
    } else {
      this.pendingRide.favorite = true;
      this.pendingRide = null;
    }
  }

  // Called when remove-favorite modal confirm is emitted
  onRemoveFavoriteConfirmed(): void {
    if (!this.pendingRide) return;
    const routeId = (this.pendingRide as any).routeId;
    if (routeId) {
      this.passengerService.removeFavorite(routeId).subscribe({
        next: () => {
          // mark all rides that share this routeId as not favorite
          try {
            this._rides.forEach(r => { if (r.routeId === routeId) r.favorite = false; });
          } catch (e) {}
          this.pendingRide = null;
          try { this.cdr.detectChanges(); } catch (e) {}
        },
        error: (e) => {
          console.warn('Failed to remove favorite', e);
          this.pendingRide = null;
          try { this.cdr.detectChanges(); } catch (e) {}
        }
      });
    } else {
      this.pendingRide.favorite = false;
      this.pendingRide = null;
    }
  }

}
