import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { ActiveRidesTable } from '../../components/active-rides-table/active-rides-table';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';

// Interface representing the structure of an active ride, including details about the ride, driver, passengers, route, and panic status.
export interface ActiveRide {
  rideId: number;
  startTime: string | null;
  driverName: string;
  passengerNames: string;
  panic: boolean;
  panicBy: string | null;
  route: {
    distanceKm: number;
    estimatedTimeMin: number;
    startLocation: {
      latitude: number;
      longitude: number;
      address: string;
    };
    endLocation: {
      latitude: number;
      longitude: number;
      address: string;
    };
    stopLocations: {
      latitude: number;
      longitude: number;
      address: string;
    }[];
    polylinePoints: {
      latitude: number;
      longitude: number;
    }[];
  };
}

@Component({
  selector: 'app-active-rides',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, ActiveRidesTable, Button, InputComponent],
  templateUrl: './active-rides.html',
  styleUrl: './active-rides.css',
})
export class ActiveRides implements OnInit {
  activeRides: ActiveRide[] = [];
  filteredRides: ActiveRide[] = [];
  loading = false;
  refreshing = false;
  error: string | null = null;
  searchQuery = '';

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadActiveRides();
  }

  // Method to load active rides from the backend using the AdminService, handles loading state, error handling, and updates the activeRides and filteredRides arrays accordingly, also triggers change detection to update the UI with the new data.
  loadActiveRides(): void {
    console.log('loadActiveRides() called');
    this.loading = true;
    this.refreshing = true;
    this.error = null;
    this.cdr.detectChanges();

    // Call the AdminService to get active rides, handles success and error responses, updates the activeRides and filteredRides arrays, manages loading and refreshing states, and triggers change detection to update the UI with the new data or error message.
    this.adminService.getActiveRides().subscribe({
      next: (response: ActiveRide[]) => {
        console.log('Active rides loaded successfully:', response);
        this.activeRides = response;
        this.applySearch();
        this.loading = false;
        this.refreshing = false;
        this.cdr.detectChanges();

        // Force UI update with setTimeout
        setTimeout(() => {
          this.cdr.detectChanges();
        }, 0);
      },
      error: (error) => {
        console.error('Error loading active rides:', error);
        console.log('Full error object:', error);
        this.error = 'Failed to load active rides. Please try again.';
        this.loading = false;
        this.refreshing = false;
        this.cdr.detectChanges();

        // Force UI update with setTimeout
        setTimeout(() => {
          this.cdr.detectChanges();
        }, 0);
      }
    });
  }

  onRefresh(): void {
    console.log('onRefresh() called, refreshing:', this.refreshing);
    if (this.refreshing) {
      console.log('Already refreshing, ignoring request');
      return;
    }
    this.loadActiveRides();
  }

  onSearchChange(value: string): void {
    this.searchQuery = value;
    this.applySearch();
  }

  // Method to apply search filtering to the active rides based on the search query, updates the filteredRides array to only include rides where the driver's name includes the search query (case-insensitive), or resets to all active rides if the search query is empty.
  applySearch(): void {
    if (!this.searchQuery.trim()) {
      this.filteredRides = [...this.activeRides];
    } else {
      this.filteredRides = this.activeRides.filter(ride =>
        ride.driverName.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.applySearch();
  }
}
