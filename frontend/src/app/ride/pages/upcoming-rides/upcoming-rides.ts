import { Component } from '@angular/core';
import { UpcomingRidesTable } from '../../components/upcoming-rides-table/upcoming-rides-table';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { UpcomingRide } from '../../components/upcoming-rides-table/upcoming-rides-table';
import { fileURLToPath } from 'url';

@Component({
  selector: 'app-upcoming-rides',
  standalone: true,
  imports: [UpcomingRidesTable, PageHeaderComponent],
  templateUrl: './upcoming-rides.html',
  styleUrl: './upcoming-rides.css',
})
export class UpcomingRides {
  allUpcomingRides: UpcomingRide[] = [
  {
    id: 1,
    route: 'Bulevar Oslobođenja 12 → Trg slobode 1',
    startTime: '12-03-2026, 18:45',
    passengers: 'Marko Marković, Ana Jovanović',
  },
  {
    id: 2,
    route: 'Liman IV, Narodnog fronta 45 → Spens',
    startTime: '10-02-2026, 21:10',
    passengers: 'Petar Petrović',
  },
  {
    id: 3,
    route: 'Železnička stanica Novi Sad → Petrovaradinska tvrđava',
    startTime: '05-01-2026, 09:15',
    passengers: 'Jovana Nikolić, Stefan Stojanović',
  },
  {
    id: 4,
    route: 'Detelinara, Branka Ćopića 18 → Univerzitet',
    startTime: '18-04-2026, 19:02',
    passengers: 'Milica Đorđević',
  },
  {
    id: 5,
    route: 'Klisa, Temerinska 102 → Trg republike',
    startTime: '05-01-2026, 09:14',
    passengers: 'Nikola Ilić, Jelena Pavlović, Dušan Stanković',
  }
];
  filteredUpcomingRides: UpcomingRide[] = [...this.allUpcomingRides]
  onFilter(filterDate: string): void {
    if(filterDate){
      filterDate = formatFilterDate(filterDate)
      this.filteredUpcomingRides = this.allUpcomingRides.filter(UpcomingRide => UpcomingRide.startTime.split(', ')[0] === filterDate);
    }else{
      this.filteredUpcomingRides = [...this.allUpcomingRides];
    }
  }
  onClearFilter(): void {
    this.filteredUpcomingRides = [...this.allUpcomingRides];
  }
}

function formatFilterDate(filterDate: string): string {
  const [year, month, day ] = filterDate.split('-');
  return day + '-' + month + '-' + year;
}