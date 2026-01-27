import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Rating, Route } from '../../../services/ride-history.service';

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
  rating?: Rating | null;
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
  }
  get rides(): Ride[] {
    return this._rides;
  }

  @Output() sortChange = new EventEmitter<{ column: string, direction: string }>();

  sortColumn: SortColumn | '' = '';
  sortDirection: SortDirection = '';

  ngOnInit(): void {
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
        this.sortChange.emit({ column: '', direction: '' });
        return;
      }
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }

    // Emit sort event to parent for server-side sorting
    this.sortChange.emit({ column: column.toLowerCase(), direction: this.sortDirection });
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
