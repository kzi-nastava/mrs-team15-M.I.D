import { Observable, tap } from "rxjs";
import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { DriverStatusStore } from "../shared/states/driver-status.store";
import { UpcomingRide } from "../ride/components/upcoming-rides-table/upcoming-rides-table";
import { environment } from "../../environments/environment";

interface ActivateResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class DriverService {

  private apiURL = environment.apiUrl + '/driver';
  private apiURLShort = environment.apiUrl;
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
    return this.http.put<any>(`${this.apiURLShort}/rides/${rideId}/start`, {});
  }

  updateLocation(lat: number, lon: number): Observable<any> {
    return this.http.put(`${this.apiURL}/update-location`, { lat, lon });
  }

  driverActivate(token: string, data: { password: string; passwordConfirmation: string; token: string }): Observable<ActivateResponse> {
    return this.http.put<ActivateResponse>(`${this.apiURL}/activate-account`, data);
  }
}
