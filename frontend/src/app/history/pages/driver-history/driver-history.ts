import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { RideHistoryTableComponent, Ride } from '../../components/ride-history-table/ride-history-table';
import { RideHistoryService, RideHistoryResponse, PaginatedRideHistoryResponse } from '../../../services/ride-history.service';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-driver-history',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent, RideHistoryTableComponent, Button],
  templateUrl: './driver-history.html',
  styleUrl: './driver-history.css',
})
export class DriverHistory implements OnInit {
  private rideHistoryService = inject(RideHistoryService);
  private cdr = inject(ChangeDetectorRef);

  allRides: Ride[] = [];
  filteredRides: Ride[] = [];

  // Pagination state
  currentPage: number = 0;
  pageSize: number = 8;
  totalPages: number = 0;
  totalElements: number = 0;
  isFirstPage: boolean = true;
  isLastPage: boolean = false;

  // Sorting state
  currentSortBy?: string;
  currentSortDir?: string;

  // Filter state
  currentFilterDate?: number;

  ngOnInit(): void {
    this.loadRideHistory();
  }

  private loadRideHistory(page: number = 0, sortBy?: string, sortDir?: string, date?: number): void {
    this.rideHistoryService.getDriverRideHistory(page, this.pageSize, sortBy, sortDir, date).subscribe({
      next: (data: PaginatedRideHistoryResponse) => {
        this.allRides = this.transformRideData(data.content);
        this.filteredRides = [...this.allRides];

        // Update pagination state
        this.currentPage = data.number;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isFirstPage = data.first;
        this.isLastPage = data.last;

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
      // Convert date string (YYYY-MM-DD) to timestamp (Long)
      const date = new Date(filterDate);
      date.setHours(0, 0, 0, 0); // Set to start of day
      this.currentFilterDate = date.getTime();
    } else {
      this.currentFilterDate = undefined;
    }
    this.currentPage = 0; // Reset to first page when filtering
    this.loadRideHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
  }

  onClearFilter(): void {
    this.currentFilterDate = undefined;
    this.currentPage = 0; // Reset to first page
    this.loadRideHistory(this.currentPage, this.currentSortBy, this.currentSortDir);
  }

  onSort(event: { column: string, direction: string }): void {
    this.currentSortBy = event.column;
    this.currentSortDir = event.direction;
    this.currentPage = 0; // Reset to first page when sorting
    this.loadRideHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
  }

  goToNextPage(): void {
    if (!this.isLastPage) {
      this.currentPage++;
      this.loadRideHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
    }
  }

  goToPreviousPage(): void {
    if (!this.isFirstPage) {
      this.currentPage--;
      this.loadRideHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
    }
  }
}
