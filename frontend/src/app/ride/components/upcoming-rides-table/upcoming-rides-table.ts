import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CancelRideModal } from '../cancel-ride-modal/cancel-ride-modal';
import { CommonModule } from '@angular/common';
import { RideService } from '../../../services/ride.service';

export interface UpcomingRide{
  id: number;
  route: string;
  startTime: string;
  passengers: string;
  canCancel : boolean;
}

type SortColumn = 'route' | 'startTime' | 'passengers' ;
type SortDirection = 'asc' | 'desc' | '';

@Component({
  selector: 'app-upcoming-rides-table',
  standalone: true,
  imports: [CommonModule, Button, CancelRideModal],
  templateUrl: './upcoming-rides-table.html',
  styleUrl: './upcoming-rides-table.css',
})
export class UpcomingRidesTable implements OnInit {
  @Output() rideCanceled = new EventEmitter<string>();

  constructor(private cdr: ChangeDetectorRef, private rideService: RideService){}

  private _upcomingRides : UpcomingRide[] = []

  @Input()
  set upcomingRides(value: UpcomingRide[]) {
    this._upcomingRides = value;
    if (this._upcomingRides.length > 0) {
      this.applySorting();
    }
    console.log(this.upcomingRides);
  }
  get upcomingRides(): UpcomingRide[] {
    return this._upcomingRides;
  }

  sortColumn: SortColumn | '' = 'startTime';
  sortDirection: SortDirection = 'desc';

  ngOnInit(): void {
    if (this._upcomingRides.length > 0) {
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

  this._upcomingRides.sort((a, b) => {
    let aValue: any = a[this.sortColumn as SortColumn];
    let bValue: any = b[this.sortColumn as SortColumn];

    if (this.sortColumn === 'startTime') {
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

  selectedRide : UpcomingRide | null = null;
  showCancelModal = false;

  openCancelModal(upcomingRide : UpcomingRide) : void {
    this.selectedRide = upcomingRide;
    this.showCancelModal = true;
  }

  onCancelConfirmed(data: { id: number; reason: string }) {
    this.rideService.cancelRide(data.id, {reason: data.reason}).subscribe({
      next: () => {
        this.rideCanceled.emit('Ride canceled successfully. Plans change — no worries!');
        this._upcomingRides = this._upcomingRides.filter(ride => ride.id !== data.id);
        this.cdr.detectChanges();
        this.applySorting();
        this.showCancelModal = false;
        if(this._upcomingRides.length == 0){
          this.rideCanceled.emit("You don’t have any scheduled rides at the moment.")
        }
      },
      error: (err) => {
        this.showCancelModal = false;
        let message = 'Ride cancellation failed. Please try again.';
        if (typeof err.error === 'string') {
          message = err.error;
        } else if (err.error?.message) {
          message = err.error.message;
        }
        this.rideCanceled.emit(message);
        this.cdr.detectChanges();
      }
    });
  }
}