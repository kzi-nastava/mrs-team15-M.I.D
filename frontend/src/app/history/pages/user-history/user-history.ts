import { Component, ChangeDetectorRef, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { UserHistoryTable } from '../../components/user-history-table/user-history-table';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { CommonModule } from '@angular/common';
import { Ride } from '../../components/user-history-table/user-history-table';
import { formatAddress } from '../../../shared/utils/address.utils';
import { Button } from '../../../shared/components/button/button';
import { HistoryService, PaginatedRideHistoryResponse, RideHistoryResponse } from '../../../services/history.service';

@Component({
  selector: 'app-user-history',
  standalone: true,
  imports: [UserHistoryTable, PageHeaderComponent, CommonModule, Button],
  templateUrl: './user-history.html',
  styleUrl: './user-history.css',
})
export class UserHistory implements OnInit  {
  allRides: Ride[] = [];
  filteredRides: Ride[] = [];
  
  currentPage: number = 0;
  pageSize: number = 8;
  totalPages: number = 0;
  totalElements: number = 0;
  isFirstPage: boolean = true;
  isLastPage: boolean = false;
  
  currentSortBy: string = 'startTime';
  currentSortDir: string = 'desc';

  
  currentFilterDate?: number;

  constructor(private historyService: HistoryService, private cdr: ChangeDetectorRef) {}
  @Input() initialSortColumn: string = '';
  @Input() initialSortDirection: 'asc' | 'desc' = 'asc';


ngOnInit(): void {
  this.loadHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
}


  private loadHistory(page: number = 0, sortBy?: string, sortDir?: string, date?: number) {
    this.historyService.getPassengerRideHistory(page, this.pageSize, sortBy, sortDir, date).subscribe({
      next: (data: PaginatedRideHistoryResponse) => {
        this.allRides = this.transformRideData(data.content);
        this.filteredRides = [...this.allRides];
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
  return apiData.map((ride) => {
    const startTime = new Date(ride.startTime);
    const endTime = new Date(ride.endTime);
    
    const durationMs = endTime.getTime() - startTime.getTime();
    const durationMinutes = durationMs > 0 ? Math.round(durationMs / (1000 * 60)) : null;
    
    return {
      id: ride.rideId ?? 0,  
      routeId: ride.routeId ?? 0,
      favorite: ride.favoriteRoute ?? false,
      route: ride.route ? `${formatAddress(ride.route.startLocation.address)} → ${formatAddress(ride.route.endLocation.address)}` : 'N/A',
      routeData: ride.route ?? null,
      passengers: ride.passengers.join(', '),
      date: startTime.toLocaleDateString('sr-RS'),
      timeRange: `${startTime.toLocaleTimeString('sr-RS', {hour: '2-digit', minute: '2-digit'})} - ${endTime.toLocaleTimeString('sr-RS', {hour: '2-digit', minute: '2-digit'})}`,
      duration: durationMinutes !== null ? `${durationMinutes} min` : 'N/A',
      cancelled: ride.cancelled ? `By ${ride.cancelledBy ?? 'unknown'}` : null,
      cancelledBy: ride.cancelledBy,
      panicButton: ride.panic ? `By ${ride.panicBy ?? 'unknown'}` : null,
      panicBy: ride.panicBy, 
      cost: `${Math.round(ride.cost)} RSD`, 
      rating: ride.rating ?? null, 
      inconsistencies: ride.inconsistencies ?? [],
      startTime: startTime.toLocaleString('sr-RS', { 
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      }),
      endTime: endTime.toLocaleString('sr-RS', { 
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      }),
      endTimeTimestamp: endTime.getTime(),  
      driver: ride.driver ?? 'N/A'
    };
  });
}
  
onFilter(filterDate: string): void {
  if (filterDate) {
    const date = new Date(filterDate);
    date.setHours(0, 0, 0, 0); 
    this.currentFilterDate = date.getTime();
  } else {
    this.currentFilterDate = undefined;
  }
  this.currentPage = 0; 
  this.loadHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
}

  onClearFilter(): void {
    this.currentFilterDate = undefined;
    this.currentPage = 0; 
    this.loadHistory(this.currentPage, this.currentSortBy, this.currentSortDir);
  }

  onSort(event: { column: string; direction: string }): void {
    this.currentSortBy = event.column;
    this.currentSortDir = event.direction;
    this.currentPage = 0; 
    this.loadHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
  }

  goToNextPage(): void {
    if (!this.isLastPage) {
      this.currentPage++;
      this.loadHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
    }
  }

  goToPreviousPage(): void {
    if (!this.isFirstPage) {
      this.currentPage--;
      this.loadHistory(this.currentPage, this.currentSortBy, this.currentSortDir, this.currentFilterDate);
    }
  }
}