import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { formatAddress } from '../../../shared/utils/address.utils';
import { ActiveRide } from '../../pages/active-rides/active-rides';

@Component({
  selector: 'app-active-rides-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './active-rides-table.html',
  styleUrl: './active-rides-table.css',
})
export class ActiveRidesTable {
  @Input() rides: ActiveRide[] = [];
  @Output() refresh = new EventEmitter<void>();

  constructor(private router: Router) {}

  formatRoute(ride: ActiveRide): string {
    const shortStart = formatAddress(ride.route.startLocation.address);
    const shortEnd = formatAddress(ride.route.endLocation.address);
    return `${shortStart} → ${shortEnd}`;
  }

  getElapsedTime(startTime: string | null): string {
    if (!startTime) {
      return 'Not started';
    }

    const start = new Date(startTime);
    const now = new Date();
    const diffMs = now.getTime() - start.getTime();
    const diffMinutes = Math.floor(diffMs / (1000 * 60));

    if (diffMinutes < 1) {
      return 'Just started';
    } else if (diffMinutes < 60) {
      return `${diffMinutes} min`;
    } else {
      const hours = Math.floor(diffMinutes / 60);
      const minutes = diffMinutes % 60;
      return `${hours}h ${minutes}m`;
    }
  }

  getDurationText(startTime: string | null, ride: ActiveRide): { elapsed: string; estimated: string } {
    const elapsed = this.getElapsedTime(startTime);
    const estimated = `${Math.round(ride.route.estimatedTimeMin)} min estimated`;
    
    return { elapsed, estimated };
  }

  getPanicBadgeClass(panic: boolean): string {
    return panic ? 'bg-danger' : 'bg-success';
  }

  getPanicText(panic: boolean, panicBy: string | null): string {
    if (panic && panicBy) {
      return `Panic - ${panicBy}`;
    } else if (panic) {
      return 'Panic Alert';
    }
    return 'Normal';
  }

  onRefreshClick(): void {
    this.refresh.emit();
  }

  getCurrentTime(): Date {
    return new Date();
  }

  onRideClick(ride: ActiveRide): void {
    this.router.navigate(['/current-ride'], { 
      state: { 
        ride: ride,
        fromAdmin: true,
        isPanic: ride.panic
      } 
    });
  }
}