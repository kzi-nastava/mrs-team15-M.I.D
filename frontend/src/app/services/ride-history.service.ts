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
  polylinePoints: Location[];
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
  startTime: string;
  endTime: string;
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

  // Method to get the ride history for a driver, accepts pagination parameters (page and size),
  // optional sorting parameters (sortBy and sortDir), and an optional date filter,
  // constructs the API URL with the provided parameters and makes an HTTP GET request
  // to retrieve the paginated ride history response from the backend
  getDriverRideHistory(page: number = 0, size: number = 8, sortBy?: string, sortDir?: string, date?: number): Observable<PaginatedRideHistoryResponse> {
    let url = `${this.apiUrl}/driver/ride-history?page=${page}&size=${size}`;
    if (sortBy && sortDir) {
      url += `&sortBy=${sortBy}&sortDir=${sortDir}`;
    }
    if (date !== undefined && date !== null) {
      url += `&date=${date}`;
    }
    return this.http.get<PaginatedRideHistoryResponse>(url);
  }
}
