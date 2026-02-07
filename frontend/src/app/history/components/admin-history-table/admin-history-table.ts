import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { Rating, Route } from '../../../services/history.service';

export interface Ride {
  id: number;
  routeId: number;
  favorite: boolean;
  route: string;
  routeData: Route | null;
  passengers: string;
  date: string;
  timeRange: string;
  duration: string;
  cancelled: string | null;
  cancelledBy: string | null;
  panicButton: string | null;
  panicBy: string | null;
  cost: string;
  rating: Rating | null;  
  inconsistencies: string[];
  startTime: string;
  endTime: string;
  driver: string;
}

type SortColumn = 'route' | 'startTime' | 'endTime' | 'cancelled' | 'cost' | 'panicButton'; 
type SortDirection = 'asc' | 'desc' | '';


@Component({
  selector: 'app-admin-history-table',
  imports: [],
  templateUrl: './admin-history-table.html',
  styleUrl: './admin-history-table.css',
})
export class AdminHistoryTable {
 private _rides : Ride[] = []

  constructor(private router: Router) {}

  @Input()
  set rides(value: Ride[]) {
    this._rides = value;
    if (this._rides.length > 0) {
      this.applySorting();
    }
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

      else if (this.sortColumn === 'cost') {
        aValue = parseFloat(a.cost.replace(/[^0-9.-]/g, '')) || 0;
        bValue = parseFloat(b.cost.replace(/[^0-9.-]/g, '')) || 0;
      }

      else if (aValue === null || aValue === undefined) {
        return this.sortDirection === 'asc' ? 1 : -1;
      } else if (bValue === null || bValue === undefined) {
        return this.sortDirection === 'asc' ? -1 : 1;
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
}