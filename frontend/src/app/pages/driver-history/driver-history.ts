import { Component } from '@angular/core';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { RideHistoryTableComponent, Ride } from '../../shared/components/ride-history-table/ride-history-table';

@Component({
  selector: 'app-driver-history',
  standalone: true,
  imports: [PageHeaderComponent, RideHistoryTableComponent],
  templateUrl: './driver-history.html',
  styleUrl: './driver-history.css',
})
export class DriverHistory {
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
      panicButton: null,
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
      panicButton: null,
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
      panicButton: "Od strane putnika",
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
      panicButton: null,
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
      panicButton: null,
      panicBy: null
    }
  ];

  filteredRides: Ride[] = [...this.allRides];

  onFilter(filterDate: string): void {
    if (filterDate) {
      this.filteredRides = this.allRides.filter(ride => ride.date === filterDate);
    } else {
      this.filteredRides = [...this.allRides];
    }
  }

  onClearFilter(): void {
    this.filteredRides = [...this.allRides];
  }
}
