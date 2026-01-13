import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { RideHistoryTableComponent, Ride } from '../../components/ride-history-table/ride-history-table';
import { RideHistoryService, RideHistoryResponse } from '../../../services/ride-history.service';

@Component({
  selector: 'app-driver-history',
  standalone: true,
  imports: [PageHeaderComponent, RideHistoryTableComponent],
  templateUrl: './driver-history.html',
  styleUrl: './driver-history.css',
})
export class DriverHistory implements OnInit {
  private rideHistoryService = inject(RideHistoryService);
  private cdr = inject(ChangeDetectorRef);

  allRides: Ride[] = [];
  filteredRides: Ride[] = [];

  ngOnInit(): void {
    this.loadRideHistory();
  }

  private loadRideHistory(): void {
    // TODO: Get actual driver ID from auth service
    const driverId = 1;

    this.rideHistoryService.getDriverRideHistory(driverId).subscribe({
      next: (data: RideHistoryResponse[]) => {
        this.allRides = this.transformRideData(data);
        this.filteredRides = [...this.allRides];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading ride history:', error);
      }
    });
  }

  private transformRideData(apiData: RideHistoryResponse[]): Ride[] {
    return apiData.map((ride, index) => ({
      id: index + 1,
      route: ride.route
        ? `${ride.route.startLocation.address} → ${ride.route.endLocation.address}`
        : 'N/A',
      passengers: ride.passengers.join(', '),
      date: ride.date,
      duration: ride.durationMinutes > 0 ? `${ride.durationMinutes} min` : 'N/A',
      timeRange: 'N/A', // Not provided by API
      cancelled: ride.cancelled ? (ride.cancelledBy ? `Od strane putnika` : 'Od strane vozača') : null,
      cancelledBy: ride.cancelledBy,
      cost: `${ride.cost.toFixed(0)} RSD`,
      panicButton: ride.panic ? (ride.panicBy ? `Od strane Putnika` : 'Od strane vozača') : null,
      panicBy: ride.panicBy,
      rating: ride.rating,
      inconsistencies: ride.inconsistencies,
      routeData: ride.route
    }));
  }

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
