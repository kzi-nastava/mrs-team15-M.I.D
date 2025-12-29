import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, interval } from 'rxjs';
import { Vehicle } from '../model/vehicle.model';

@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private vehiclesSubject = new BehaviorSubject<Vehicle[]>([]);
  public vehicles$: Observable<Vehicle[]> = this.vehiclesSubject.asObservable();

  private vehicles: Vehicle[] = [];
  private movementSubscription: any;

  constructor() {}

  // Initialize vehicles around a location
  initializeVehicles(centerLat: number, centerLng: number, count: number = 10): void {
    this.vehicles = [];

    for (let i = 0; i < count; i++) {
      const vehicle: Vehicle = {
        id: i,
        lat: centerLat + (Math.random() - 0.5) * 0.02, // Random offset ~1km
        lng: centerLng + (Math.random() - 0.5) * 0.02,
        available: Math.random() > 0.3 // 70% available
      };

      this.vehicles.push(vehicle);
    }

    this.vehiclesSubject.next([...this.vehicles]);
    this.startVehicleMovement();
  }

  // Start simulating vehicle movement
  private startVehicleMovement(): void {
    // Stop existing movement if any
    this.stopVehicleMovement();

    // Update vehicle positions every 2 seconds
    this.movementSubscription = interval(2000).subscribe(() => {
      this.vehicles.forEach(vehicle => {
        // Random small movement (simulating driving)
        const latChange = (Math.random() - 0.5) * 0.001; // ~100m
        const lngChange = (Math.random() - 0.5) * 0.001;

        vehicle.lat += latChange;
        vehicle.lng += lngChange;
      });

      // Emit updated vehicles
      this.vehiclesSubject.next([...this.vehicles]);
    });
  }

  // Stop vehicle movement simulation
  stopVehicleMovement(): void {
    if (this.movementSubscription) {
      this.movementSubscription.unsubscribe();
      this.movementSubscription = null;
    }
  }

  // Get current vehicles
  getVehicles(): Vehicle[] {
    return [...this.vehicles];
  }

  // Update vehicle availability
  updateVehicleAvailability(vehicleId: number, available: boolean): void {
    const vehicle = this.vehicles.find(v => v.id === vehicleId);
    if (vehicle) {
      vehicle.available = available;
      this.vehiclesSubject.next([...this.vehicles]);
    }
  }
}
