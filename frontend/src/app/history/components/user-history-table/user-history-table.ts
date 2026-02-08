import { Component, Input, ViewChild, ChangeDetectorRef, EventEmitter, Output } from '@angular/core';
import { Router } from '@angular/router';
import { PassengerService } from '../../../services/passenger.service';
import { CommonModule } from '@angular/common';
import { AddFavoriteModal } from '../add-favorite-modal/add-favorite-modal';
import { RemoveFavoriteModal } from '../remove-favorite-modal/remove-favorite-modal';
import { Button } from '../../../shared/components/button/button';

export interface Ride {
  id: number;
  routeId: number;
  route: string;
  routeData: any;  
  passengers: string;
  date: string;
  timeRange: string;
  duration: string;
  cancelled: string | null;
  cancelledBy: string | null;
  panicButton: string | null;
  panicBy: string | null;
  price: string;
  rating: { 
    driverRating: number; 
    vehicleRating: number; 
    driverComment: string; 
    vehicleComment: string; 
  } | null;  
  inconsistencies: string[];
  startTime: string;
  endTime: string;
  driver: string;
  
  favorite?: boolean;  
  pickupAddress?: string | null;  
  destinationAddress?: string | null;
  stopAddress?: string | null;  
  stopAddresses?: string[] | null;
  endTimeTimestamp: number;   
}


type SortColumn = 'route' | 'startTime' | 'endTime' ;

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

  @Input() rides: Ride[] = [];
  @Output() sortChange = new EventEmitter<{ column: string; direction: string }>();

  currentSortColumn: string = '';
  currentSortDirection: 'asc' | 'desc' = 'asc';

  sort(column: string): void {
    if (this.currentSortColumn === column) {
      this.currentSortDirection = this.currentSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.currentSortColumn = column;
      this.currentSortDirection = 'asc';
    }
    
    this.sortChange.emit({ 
      column: this.currentSortColumn, 
      direction: this.currentSortDirection 
    });
  }

  getSortIcon(column: SortColumn): string {
    if (this.currentSortColumn !== column) {
      return '⇅';
    }
    return this.currentSortDirection === 'asc' ? '↑' : '↓';
  }

  viewRideDetails(ride: Ride): void {
    this.router.navigate(['/history-ride-details', ride.id], { state: { ride } });
  }

  rateRide(ride: Ride): void {
    this.router.navigate(['/rating', ride.id]);
  }

canRate(ride: Ride): boolean {
  if (ride.rating || ride.cancelled) {
    return false;
  }
  const now = Date.now();
  const threeDaysInMs = 3 * 24 * 60 * 60 * 1000;
  const timeDiff = now - ride.endTimeTimestamp;
  return timeDiff <= threeDaysInMs;
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
