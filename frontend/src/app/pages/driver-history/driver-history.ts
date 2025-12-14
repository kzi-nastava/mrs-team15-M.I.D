import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Ride {
  route: string;
  passengers: string;
  date: string;
  duration: string;
  timeRange: string;
  cancelled: string | null;
  cancelledBy: string | null;
  cost: string;
  panicButton: boolean;
  panicBy: string | null;
}

@Component({
  selector: 'app-driver-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './driver-history.html',
  styleUrl: './driver-history.css',
})
export class DriverHistory {
filterDate: string = '';

  allRides: Ride[] = [
    {
      route: 'Main St, Belgrade → Airport Rd, Belgrade',
      passengers: 'John Doe, Jane Smith',
      date: '2025-12-12',
      duration: '25 min',
      timeRange: '14:30 - 14:55',
      cancelled: null,
      cancelledBy: null,
      cost: '$15.50',
      panicButton: false,
      panicBy: null
    },
    {
      route: 'City Center → Train Station',
      passengers: 'Mike Johnson',
      date: '2025-12-11',
      duration: '12 min',
      timeRange: '09:15 - 09:27',
      cancelled: 'By Passenger',
      cancelledBy: 'Mike Johnson',
      cost: '$8.00',
      panicButton: false,
      panicBy: null
    },
    {
      route: 'Park Avenue → Shopping Mall',
      passengers: 'Sarah Williams, Tom Brown',
      date: '2025-12-12',
      duration: '18 min',
      timeRange: '16:00 - 16:18',
      cancelled: null,
      cancelledBy: null,
      cost: '$12.75',
      panicButton: true,
      panicBy: 'Sarah Williams'
    },
    {
      route: 'Hotel Plaza → Conference Center',
      passengers: 'Emily Davis',
      date: '2025-12-10',
      duration: '30 min',
      timeRange: '11:00 - 11:30',
      cancelled: 'By Driver',
      cancelledBy: null,
      cost: '$18.00',
      panicButton: false,
      panicBy: null
    },
    {
      route: 'Downtown → Riverside Park',
      passengers: 'Alex Martinez, Chris Lee, Pat Wilson',
      date: '2025-12-12',
      duration: '22 min',
      timeRange: '13:45 - 14:07',
      cancelled: null,
      cancelledBy: null,
      cost: '$20.50',
      panicButton: false,
      panicBy: null
    }
  ];

  filteredRides: Ride[] = [...this.allRides];

  filterRides(): void {
    if (this.filterDate) {
      this.filteredRides = this.allRides.filter(ride => ride.date === this.filterDate);
    } else {
      this.filteredRides = [...this.allRides];
    }
  }

  clearFilter(): void {
    this.filterDate = '';
    this.filteredRides = [...this.allRides];
  }

  formatDate(date: string): string {
    const d = new Date(date);
    return `${d.getDate()}-${d.getMonth() + 1}-${d.getFullYear()}`;
  }
}
