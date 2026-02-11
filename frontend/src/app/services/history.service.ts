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
  rideId: number | null;
  driver: string | null;
  route: Route | null;
  routeId: number | null;
  passengers: string[];
  startTime: string;
  endTime: string;
  price: number;
  cancelled: boolean;
  cancelledBy: string | null;
  panic: boolean | null;
  panicBy: string | null;
  rating: Rating | null;
  inconsistencies: string[];
  favoriteRoute: boolean | null;
  endTimeTimestamp: number;
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

export interface UserResponse {
  id: number | null;
  name: string | null;
  surname: string | null;
  role: string | null;
  email: string | null;
}

export interface PaginatedUsersResponse {
  content: UserResponse[];
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
export class HistoryService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

getPassengerRideHistory(page: number = 0, size: number = 8, sortBy?: string, sortDir?: string, date?: number): Observable<PaginatedRideHistoryResponse> {
    let url = `${this.apiUrl}/passengers/ride-history?page=${page}&size=${size}`; 
    
    if (sortBy && sortDir) {
        url += `&sortBy=${sortBy}&sortDir=${sortDir}`;  
    }
    if (date !== undefined && date !== null) {
        url += `&date=${date}`;
    }
    return this.http.get<PaginatedRideHistoryResponse>(url);
}

getUsers(page: number = 0, size: number = 8, sortBy?: string, sortDir?: string) : Observable<PaginatedUsersResponse> {
    let url = `${this.apiUrl}/admins/all-users?page=${page}&size=${size}`; 
    if (sortBy && sortDir) {
        url += `&sortBy=${sortBy}&sortDir=${sortDir}`;  
    }
    return this.http.get<PaginatedUsersResponse>(url);
  }

  getAdminRideHistory(id: number, page: number = 0, size: number = 8, sortBy?: string, sortDir?: string, date?: number): Observable<PaginatedRideHistoryResponse> {
    let url = `${this.apiUrl}/admins/ride-history?id=${id}&page=${page}&size=${size}`;
    if (sortBy && sortDir) {
      url += `&sortBy=${sortBy}&sortDir=${sortDir}`;
    }
    if(date !== undefined && date !== null) {
      url += `&date=${date}`;
    }
    return this.http.get<PaginatedRideHistoryResponse>(url);
  }
}