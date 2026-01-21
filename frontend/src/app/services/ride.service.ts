import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrentRide } from '../ride/pages/current-ride/current-ride';
import { CurrentRideDTO } from '../ride/components/current-ride-form/current-ride-form';
import { environment } from '../../environments/environment';
import { Observable, lastValueFrom } from 'rxjs';
import { HttpParams } from '@angular/common/http';
import { UpcomingRide } from '../ride/components/upcoming-rides-table/upcoming-rides-table';

interface ActivateResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class RideService {
  private apiURL = environment.apiUrl + '/rides';

  constructor(private http: HttpClient) {}

  // Simple geocode using Nominatim (no API key). Returns {lat, lon} or null.
  async geocodeAddress(address: string): Promise<{ lat: number; lon: number } | null> {
    if (!address) return null;
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`
      );
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        return { lat: parseFloat(data[0].lat), lon: parseFloat(data[0].lon) };
      }
    } catch (e) {
      console.warn('Geocode failed', e);
    }
    return null;
  }

  estimateRoute(dto: any): Promise<any> {
    return lastValueFrom(this.http.post<any>(`${this.apiURL}/estimate-route`, dto));
  }
  
  estimateRouteGet(data: { startAddress: string; destinationAddress: string; stopAddresses?: string[] }) {
    let params = new HttpParams()
      .set('startAddress', data.startAddress)
      .set('destinationAddress', data.destinationAddress);

    if (data.stopAddresses && data.stopAddresses.length) {
      // append multiple stopAddresses params
      for (const s of data.stopAddresses) {
        params = params.append('stopAddresses', s);
      }
    }

    return lastValueFrom(this.http.get<any>(`${this.apiURL}/estimate-route`, { params }));
  }

  orderRide(dto: any): Promise<any> {
    return lastValueFrom(this.http.post<any>(`${this.apiURL}`, dto));
  }

  estimate(data: { startAddress: string; destinationAddress: string }) {
    return this.http.get<any>(`${this.apiURL}/estimate`, {
      params: {
        startAddress: data.startAddress,
        destinationAddress: data.destinationAddress
      }
    });
  }

  getMyUpcomingRides(): Observable<UpcomingRide[]> {
    return this.http.get<UpcomingRide[]>(`${this.apiURL}/my-upcoming-rides`);
  }

  cancelRide(id : number,  data: { reason: string; }) {
    return this.http.put<ActivateResponse>(`${this.apiURL}/${id}/cancel`, data);
  }

    getMyCurrentRide(): Observable<CurrentRideDTO> {
    return this.http.get<CurrentRideDTO>(`${this.apiURL}/my-current-ride`);
  }

  triggerPanicAlert() {
    return this.http.post(`${this.apiURL}/panic-alert`, null);
  }

  rateRide(id: number, rating: {
    driverRating: number;
    vehicleRating: number;
    driverComment: string;
    vehicleComment: string;
  }) {
    return this.http.post<any>(`${this.apiURL}/${id}/rate`, rating);
  }

  reportInconsistency(data: { rideId: number; description: string; }) {
    return this.http.post<any>(`${this.apiURL}/inconsistency`, data);
  }

  finishRide(rideId: number): Observable<boolean> {
    return this.http.post<boolean>(`${this.apiURL}/${rideId}/finish`, {});
  }
  
  trackRide(rideId: number): Observable<{
    location: { latitude: number; longitude: number; address: string | null };
    remainingTimeInMinutes: number;
  }> {
    return this.http.get<any>(`${this.apiURL}/${rideId}/track`);
  }

  stopRide() {
    return this.http.put(`${this.apiURL}/stop`, null);
  }
}
