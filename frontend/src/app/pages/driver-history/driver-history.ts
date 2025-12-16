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
      route: 'Bulevar oslobođenja, Novi Sad → Aerodrom Nikola Tesla, Beograd',
      passengers: 'Marko Marković, Ana Jovanović',
      date: '2025-12-12',
      duration: '25 min',
      timeRange: '14:30 - 14:55',
      cancelled: null,
      cancelledBy: null,
      cost: '1550 RSD',
      panicButton: false,
      panicBy: null
    },
    {
      route: 'Trg slobode → Železnička stanica',
      passengers: 'Petar Petrović',
      date: '2025-12-11',
      duration: '12 min',
      timeRange: '09:15 - 09:27',
      cancelled: 'Od strane putnika',
      cancelledBy: 'Petar Petrović',
      cost: '800 RSD',
      panicButton: false,
      panicBy: null
    },
    {
      route: 'Liman 3 → Promenada Shopping',
      passengers: 'Jovana Nikolić, Stefan Stojanović',
      date: '2025-12-12',
      duration: '18 min',
      timeRange: '16:00 - 16:18',
      cancelled: null,
      cancelledBy: null,
      cost: '1275 RSD',
      panicButton: true,
      panicBy: 'Jovana Nikolić'
    },
    {
      route: 'Hotel Park → Spens',
      passengers: 'Milica Đorđević',
      date: '2025-12-10',
      duration: '30 min',
      timeRange: '11:00 - 11:30',
      cancelled: 'Od strane vozača',
      cancelledBy: null,
      cost: '1800 RSD',
      panicButton: false,
      panicBy: null
    },
    {
      route: 'Centar → Štrand',
      passengers: 'Nikola Ilić, Jelena Pavlović, Dušan Stanković',
      date: '2025-12-12',
      duration: '22 min',
      timeRange: '13:45 - 14:07',
      cancelled: null,
      cancelledBy: null,
      cost: '2050 RSD',
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
