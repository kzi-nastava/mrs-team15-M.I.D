import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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

export interface Rating {
  driverRating: number;
  vehicleRating: number;
  driverComment: string;
  vehicleComment: string;
}

export interface RideHistoryResponse {
  route: Route | null;
  passengers: string[];
  date: string;
  durationMinutes: number;
  cost: number;
  cancelled: boolean;
  cancelledBy: string | null;
  panic: boolean | null;
  panicBy: string | null;
  rating: Rating | null;
  inconsistencies: string[];
}

export interface PaginatedRideHistoryResponse {
  content: RideHistoryResponse[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  numberOfElements: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  empty: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class RideHistoryService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getDriverRideHistory(page: number = 0, size: number = 8, sortBy?: string, sortDir?: string): Observable<PaginatedRideHistoryResponse> {
    let url = `${this.apiUrl}/driver/ride-history?page=${page}&size=${size}`;
    if (sortBy && sortDir) {
      url += `&sortBy=${sortBy}&sortDir=${sortDir}`;
    }
    return this.http.get<PaginatedRideHistoryResponse>(url);
  }
}
