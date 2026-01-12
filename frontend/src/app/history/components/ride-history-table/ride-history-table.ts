import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Route } from '../../../services/ride-history.service';

export interface Ride {
  id: number;
  route: string;
  passengers: string;
  date: string;
  duration: string;
  timeRange: string;
  cancelled: string | null;
  cancelledBy: string | null;
  cost: string;
  panicButton: string | null;
  panicBy: string | null;
  rating?: number | null;
  inconsistencies?: string[] | null;
  routeData?: Route | null;
}

type SortColumn = 'route' | 'passengers' | 'date' | 'duration' | 'cancelled' | 'cost' | 'panicButton';
type SortDirection = 'asc' | 'desc' | '';

@Component({
  selector: 'app-ride-history-table',
  standalone: true,
  imports: [],
  templateUrl: './ride-history-table.html',
  styleUrl: './ride-history-table.css'
})
export class RideHistoryTableComponent implements OnInit {
  private _rides: Ride[] = [];

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

  sortColumn: SortColumn | '' = 'date';
  sortDirection: SortDirection = 'desc';

  ngOnInit(): void {
    if (this._rides.length > 0) {
      this.applySorting();
    }
  }

  formatDate(date: string): string {
    const d = new Date(date);
    return `${d.getDate()}-${d.getMonth() + 1}-${d.getFullYear()}`;
  }

  sort(column: SortColumn): void {
    if (this.sortColumn === column) {
      // Toggle direction: asc -> desc -> no sort
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
    if (!this.sortColumn) return;

    const column = this.sortColumn as SortColumn;
    this._rides.sort((a, b) => {
      let aValue: any = a[column];
      let bValue: any = b[column];

      // Handle date sorting
      if (column === 'date') {
        aValue = new Date(a.date).getTime();
        bValue = new Date(b.date).getTime();
      }
      // Handle cost sorting (remove currency symbols)
      else if (column === 'cost') {
        aValue = parseFloat(a.cost.replace(/[^0-9.-]/g, '')) || 0;
        bValue = parseFloat(b.cost.replace(/[^0-9.-]/g, '')) || 0;
      }
      // Handle null values
      else if (aValue === null || aValue === undefined) {
        return this.sortDirection === 'asc' ? 1 : -1;
      } else if (bValue === null || bValue === undefined) {
        return this.sortDirection === 'asc' ? -1 : 1;
      }
      // String comparison
      else if (typeof aValue === 'string' && typeof bValue === 'string') {
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
    this.router.navigate(['/ride-details', ride.id], { state: { ride } });
  }
}
