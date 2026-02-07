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

  private rides: Ride[] = [];

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