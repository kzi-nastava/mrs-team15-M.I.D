import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Ride } from '../../components/admin-history-table/admin-history-table';
import { Button } from '../../../shared/components/button/button';
import { ReorderRideModal } from '../../components/reorder-ride-modal/reorder-ride-modal';

@Component({
  selector: 'app-history-ride-details',
  standalone: true,
  imports: [CommonModule, Button, ReorderRideModal],
  templateUrl: './history-ride-details.html',
  styleUrl: './history-ride-details.css',
})
export class HistoryRideDetails {
 
  ride: Ride | null = null;
  id!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    // Prefer navigation state (caller passed full ride object)
    const navState = (history && (history as any).state) ? (history as any).state : null;
    if (navState && navState.ride) {
      this.ride = navState.ride as Ride;
      return;
    }
    this.loadRide();
  }

  private loadRide(): void {
    this.ride = this.rides.find(r => r.id === this.id) ?? null;

    if (!this.ride) {
      this.router.navigate(['/admin-history']);
    }
  }

  goBack(): void {
    this.router.navigate(['/admin-history']);
  }

  private rides: Ride[] = [
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

showReorderModal = false;

  openReorderModal(): void {
    this.showReorderModal = true;
  }

  onBookNow(): void {
    this.showReorderModal = false;
  }

  onScheduleForLater(data: { date: string; time: string }): void {
    alert('Scheduled for: ' + data.date + ' at ' + data.time);
    this.showReorderModal = false;
  }
}