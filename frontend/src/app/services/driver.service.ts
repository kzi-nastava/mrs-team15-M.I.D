import { Observable, tap } from "rxjs";
import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { DriverStatusStore } from "../shared/states/driver-status.store";
import { UpcomingRide } from "../ride/components/upcoming-rides-table/upcoming-rides-table";

interface ActivateResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class DriverService {

  private apiURL = "http://localhost:8080/api/driver";
  constructor(private http: HttpClient, private driverState: DriverStatusStore) {}

  changeDriverStatus(data: { status: string }) {
    return this.http
      .put<{ status: string; pendingStatus: string }>(`${this.apiURL}/change-status`, data)
      .pipe(tap(res => this.driverState.setStatus(res.status)));
  }

  getMyStatus() {
    return this.http
      .get<{ status: string; pendingStatus: string }>(`${this.apiURL}/status`)
      .pipe(tap(res => this.driverState.setStatus(res.status)));
  }

  getUpcomingRides(): Observable<UpcomingRide[]> {
    return this.http.get<UpcomingRide[]>(`${this.apiURL}/rides`);
  }

  startRide(rideId: number) {
    return this.http.post<any>(`${this.apiURL}/rides/${rideId}/start`, {});
  }
}