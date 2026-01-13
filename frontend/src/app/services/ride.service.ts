import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class RideService {
  private apiURL = 'http://localhost:8081/api/rides';

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
}
