import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { ActiveRidesTable } from '../../components/active-rides-table/active-rides-table';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';

// Interface representing the structure of an active ride as returned by the backend
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

  // On component initialization, load the active rides from the backend
  ngOnInit(): void {
    this.loadActiveRides();
  }

  // Method to load active rides from the backend API
  loadActiveRides(): void {
    console.log('loadActiveRides() called');
    this.loading = true;
    this.refreshing = true;
    this.error = null;
    this.cdr.detectChanges();

    // Call the admin service to get active rides
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

  // Method called when the user clicks the refresh button to reload active rides
  onRefresh(): void {
    console.log('onRefresh() called, refreshing:', this.refreshing);
    if (this.refreshing) {
      console.log('Already refreshing, ignoring request');
      return;
    }
    this.loadActiveRides();
  }

  // Method called when the search input value changes, updates the search query and applies the search filter
  onSearchChange(value: string): void {
    this.searchQuery = value;
    this.applySearch();
  }

  // Method to apply the search filter to the list of active rides based on the driver's name
  applySearch(): void {
    if (!this.searchQuery.trim()) {
      this.filteredRides = [...this.activeRides];
    } else {
      this.filteredRides = this.activeRides.filter(ride =>
        ride.driverName.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    }
  }

  // Method to clear the search input and reset the filtered rides to show all active rides
  clearSearch(): void {
    this.searchQuery = '';
    this.applySearch();
  }
}
