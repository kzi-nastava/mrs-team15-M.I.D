import { ChangeDetectorRef, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Ride } from '../user-history-table/user-history-table';


type SortColumn = 'route' | 'startTime' | 'endTime' | 'cancelled' | 'price' | 'panic'; 

@Component({
  selector: 'app-admin-history-table',
  imports: [],
  templateUrl: './admin-history-table.html',
  styleUrl: './admin-history-table.css',
})
export class AdminHistoryTable {
  private _rides : Ride[] = []

  constructor(private router: Router, private cdr: ChangeDetectorRef) {}

  @Input() rides: Ride[] = [];
  @Output() sortChange = new EventEmitter<{ column: string; direction: string }>();

  currentSortColumn: string = '';
  currentSortDirection: 'asc' | 'desc' = 'asc';

  sort(column: string): void {
    if (this.currentSortColumn === column) {
      this.currentSortDirection = this.currentSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.currentSortColumn = column;
      this.currentSortDirection = 'asc';
    }
    
    this.sortChange.emit({ 
      column: this.currentSortColumn, 
      direction: this.currentSortDirection 
    });
  }

  getSortIcon(column: SortColumn): string {
    if (this.currentSortColumn !== column) {
      return '⇅';
    }
    return this.currentSortDirection === 'asc' ? '↑' : '↓';
  }

  viewRideDetails(ride: Ride): void {
    this.router.navigate(['/history-ride-details', ride.id], { state: { ride } });
  }
}