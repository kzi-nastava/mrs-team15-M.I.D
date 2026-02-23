import { Injectable, OnDestroy } from '@angular/core';
import { DriverService } from './driver.service';
import { DriverStatusStore } from '../shared/states/driver-status.store';
import { BehaviorSubject, Subscription } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LocationTrackingService implements OnDestroy {
  private locationInterval?: any;
  private statusSubscription?: Subscription;
  private currentLocationSubject = new BehaviorSubject<{ lat: number; lon: number } | null>(null);
  currentLocation$ = this.currentLocationSubject.asObservable();

  private driverLicencePlateSubject = new BehaviorSubject<string | null>(null);
  driverLicencePlate$ = this.driverLicencePlateSubject.asObservable();

  constructor(
    private driverService: DriverService,
    private driverStatusStore: DriverStatusStore
  ) {
    // Listen to driver status changes
    this.statusSubscription = this.driverStatusStore.status$.subscribe(status => {
      if (status === 'ACTIVE') {
        this.startTracking();
      } else {
        this.stopTracking();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopTracking();
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
  }

  private startTracking(): void {
    // Clear any existing interval
    this.stopTracking();

    // Send location immediately
    this.sendLocation();

    // Then send every 10 seconds
    this.locationInterval = setInterval(() => {
      this.sendLocation();
    }, 10000);
  }

  private stopTracking(): void {
    if (this.locationInterval) {
      clearInterval(this.locationInterval);
      this.locationInterval = undefined;
    }
    this.currentLocationSubject.next(null);
    this.driverLicencePlateSubject.next(null);
  }

  // Method to update the current location of the driver using the Geolocation API, updates the current location subject, and sends the location to the backend using the DriverService
  private sendLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;

          // Update the current location subject
          this.currentLocationSubject.next({ lat, lon });

          // Send to backend
          this.driverService.updateLocation(lat, lon).subscribe({
            next: (response) => {
              console.log('Location updated successfully');
              // Store the driver's license plate from the response
              if (response && response.licencePlate) {
                this.driverLicencePlateSubject.next(response.licencePlate);
              }
            },
            error: (error) => {
              console.error('Failed to update location:', error);
            }
          });
        },
        (error) => {
          console.error('Error getting location:', error);
        },
        {
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 0
        }
      );
    } else {
      console.error('Geolocation is not supported by this browser.');
    }
  }
}
