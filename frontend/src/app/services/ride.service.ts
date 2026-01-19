import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrentRide } from '../ride/pages/current-ride/current-ride';
import { CurrentRideDTO } from '../ride/components/current-ride-form/current-ride-form';
import { Observable } from 'rxjs';
import { UpcomingRide } from '../ride/components/upcoming-rides-table/upcoming-rides-table';

interface ActivateResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class RideService {
  private apiURL = 'http://localhost:8080/api/rides';

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

  estimateRoute(dto: any) {
    return this.http.post<any>(`${this.apiURL}/estimate-route`, dto).toPromise();
  }

  orderRide(dto: any) {
    return this.http.post<any>(`${this.apiURL}`, dto).toPromise();
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
}
