import { Component } from '@angular/core';
import { UserHistoryTable } from '../../components/user-history-table/user-history-table';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { Ride } from '../../components/user-history-table/user-history-table';

@Component({
  selector: 'app-user-history',
  standalone: true,
  imports: [UserHistoryTable, PageHeaderComponent],
  templateUrl: './user-history.html',
  styleUrl: './user-history.css',
})
export class UserHistory  {
allRides: Ride[] = [
  {
    id: 1,
    route: 'Bulevar Oslobođenja 12 → Trg slobode 1',
    startTime: '12-03-2026, 18:45',
    endTime: '12-03-2026, 19:10',
    passengers: 'Marko Marković, Ana Jovanović',
    driver: 'Petar Petrović',
    cancelled: null,
    cancelledBy: null,
    cost: '850 RSD',
    panicButton: null,
    panicBy: null,
    rating: 5,
    inconsistencies: null,
  },
  {
    id: 2,
    route: 'Liman IV, Narodnog fronta 45 → Spens',
    startTime: '10-02-2026, 21:10',
    endTime: '10-02-2026, 21:35',
    passengers: 'Jelena Ilić',
    driver: 'Milan Jovanović',
    cancelled: null,
    cancelledBy: null,
    cost: '620 RSD',
    panicButton: null,
    panicBy: null,
    rating: 4,
    inconsistencies: null,
  },
  {
    id: 3,
    route: 'Železnička stanica Novi Sad → Petrovaradinska tvrđava',
    startTime: '05-01-2026, 09:15',
    endTime: '05-01-2026, 09:40',
    passengers: 'Jovana Nikolić, Stefan Stojanović',
    driver: 'Nikola Stanković',
    cancelled: null,
    cancelledBy: null,
    cost: '780 RSD',
    panicButton: null,
    panicBy: null,
    rating: 5,
    inconsistencies: null,
  },
  {
    id: 4,
    route: 'Detelinara, Branka Ćopića 18 → Univerzitet',
    startTime: '18-04-2026, 19:02',
    endTime: '18-04-2026, 19:02',
    passengers: 'Milica Đorđević',
    driver: 'Aleksandar Kovačević',
    cancelled: 'Driver unavailable',
    cancelledBy: 'DRIVER',
    cost: '0 RSD',
    panicButton: null,
    panicBy: null,
    rating: null,
    inconsistencies: ['Ride cancelled after driver assignment'],
  },
  {
    id: 5,
    route: 'Klisa, Temerinska 102 → Trg republike',
    startTime: '05-01-2026, 09:14',
    endTime: '05-01-2026, 09:50',
    passengers: 'Nikola Ilić, Jelena Pavlović, Dušan Stanković',
    driver: 'Marko Radulović',
    cancelled: null,
    cancelledBy: null,
    cost: '1,150 RSD',
    panicButton: 'Emergency button pressed during ride',
    panicBy: 'PASSENGER',
    rating: 3,
    inconsistencies: ['Panic button activated', 'Ride duration longer than expected'],
  },
];
  filteredRides: Ride[] = [...this.allRides]
  onFilter(filterDate: string): void {
    if(filterDate){
      filterDate = formatFilterDate(filterDate)
      this.filteredRides = this.allRides.filter(ride => ride.startTime.split(', ')[0] === filterDate);
    }else{
      this.filteredRides = [...this.allRides];
    }
  }
  onClearFilter(): void {
    this.filteredRides = [...this.allRides];
  }
}

function formatFilterDate(filterDate: string): string {
  const [year, month, day ] = filterDate.split('-');
  return day + '-' + month + '-' + year;
}