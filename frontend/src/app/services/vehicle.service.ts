import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, Subscription } from 'rxjs';
import { Vehicle } from '../model/vehicle.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private vehiclesSubject = new BehaviorSubject<Vehicle[]>([]);
  public vehicles$: Observable<Vehicle[]> = this.vehiclesSubject.asObservable();

  private apiURL = environment.apiUrl + '/vehicles';
  private updateSubscription?: Subscription;

  constructor(private http: HttpClient) {}

  initializeVehicles(centerLat: number, centerLng: number): void {
    if (this.updateSubscription) {
      this.updateSubscription.unsubscribe();
    }

    this.updateSubscription = interval(1000).subscribe(() => {
      console.log('Fetching vehicles around', centerLat, centerLng);
      this.http.get<any[]>(`${this.apiURL}/?lat=${centerLat}&lon=${centerLng}`).subscribe({
        next: (response) => {
          const vehicles = response.map(v => ({
            licencePlate: v.licencePlate,
            lat: v.location.latitude,
            lng: v.location.longitude,
            available: v.available
          }));
          this.vehiclesSubject.next(vehicles);
        },
        error: (err) => {
          console.error('Error fetching vehicles:', err);
        }
      });
    });
  }
}
