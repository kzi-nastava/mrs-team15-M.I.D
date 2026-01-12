import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Location {
  latitude: number;
  longitude: number;
  address: string;
}

export interface Route {
  id: number | null;
  distanceKm: number;
  estimatedTimeMin: number;
  startLocation: Location;
  endLocation: Location;
  stopLocations: Location[];
}

export interface RideHistoryResponse {
  route: Route | null;
  passengers: string[];
  date: string;
  durationMinutes: number;
  cost: number;
  cancelled: boolean;
  cancelledBy: string | null;
  panic: boolean;
  panicBy: string | null;
  rating: number | null;
  inconsistencies: string[];
}

@Injectable({
  providedIn: 'root'
})
export class RideHistoryService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api';

  getDriverRideHistory(driverId: number): Observable<RideHistoryResponse[]> {
    return this.http.get<RideHistoryResponse[]>(`${this.apiUrl}/driver/${driverId}/ride-history`);
  }
}
