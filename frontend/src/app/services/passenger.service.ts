import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PassengerService {
  private apiURL = environment.apiUrl + '/passengers';

  constructor(private http: HttpClient) {}

  // GET /passengers/routes?favorite=true
  getRoutes(favorite: boolean = false): Observable<any[]> {
    let params = new HttpParams();
    if (favorite) params = params.set('favorite', 'true');
    return this.http.get<any[]>(`${this.apiURL}/routes`, { params });
  }

  // GET /passengers/favorite-routes (returns routes for authenticated passenger)
  getFavoriteRoutes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiURL}/favorite-routes`);
  }

  // PUT /passengers/favorite-routes/{routeId}
  addFavorite(routeId: number) {
    return this.http.put<any>(`${this.apiURL}/favorite-routes/${routeId}`, {});
  }

  // DELETE /passengers/favorite-routes/{routeId}
  removeFavorite(routeId: number) {
    return this.http.delete<void>(`${this.apiURL}/favorite-routes/${routeId}`);
  }
}
