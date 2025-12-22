import { Component, Input } from '@angular/core';

export interface Ride {
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
}

@Component({
  selector: 'app-ride-history-table',
  standalone: true,
  imports: [],
  templateUrl: './ride-history-table.html',
  styleUrl: './ride-history-table.css'
})
export class RideHistoryTableComponent {
  @Input() rides: Ride[] = [];

  formatDate(date: string): string {
    const d = new Date(date);
    return `${d.getDate()}-${d.getMonth() + 1}-${d.getFullYear()}`;
  }
}
