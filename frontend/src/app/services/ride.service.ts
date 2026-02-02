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

  async geocodeAddress(address: string): Promise<{ lat: number; lon: number } | null> {
    if (!address) return null;
    try {
      const res = await fetch(
        `https://photon.komoot.io/api/?q=${encodeURIComponent(address)}&limit=1`
      );
      const data = await res.json();
      if (data.features && data.features.length > 0) {
        const coords = data.features[0].geometry.coordinates;
        return { lat: coords[1], lon: coords[0] }; 
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

  estimate(data: { startAddress: string; startLatitude: number; startLongitude: number; endAddress: string; endLatitude: number; endLongitude: number;}) {
  return this.http.get<any>(`${this.apiURL}/estimate`, {
    params: {
      startAddress: data.startAddress,
      startLatitude: data.startLatitude.toString(),
      startLongitude: data.startLongitude.toString(),
      endAddress: data.endAddress,
      endLatitude: data.endLatitude.toString(),
      endLongitude: data.endLongitude.toString()
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

  // Call backend GET /rides/{id}/start which triggers passenger pickup logic
  passengerPickup(rideId: number) {
    return this.http.get<any>(`${this.apiURL}/${rideId}/start`);
  }

  stopRide() {
    return this.http.put(`${this.apiURL}/stop`, null);
  }
}
