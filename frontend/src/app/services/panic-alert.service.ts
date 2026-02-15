import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PanicAlert } from './notification-websocket.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PanicAlertService {
  private apiURL = environment.apiUrl + '/panic-alerts';
  constructor(private http: HttpClient) {}

  getUnresolvedAlerts(): Observable<PanicAlert[]> {
    return this.http.get<PanicAlert[]>(`${this.apiURL}/unresolved`);
  }

  getAllAlerts(includeResolved: boolean = false): Observable<PanicAlert[]> {
    return this.http.get<PanicAlert[]>(`${this.apiURL}/all`, {
      params: { includeResolved: includeResolved.toString() }
    });
  }

  resolvePanicAlert(id: number): Observable<any> {
    return this.http.put(`${this.apiURL}/${id}/resolve`, {});
  }

  getAlertById(id: number): Observable<PanicAlert> {
    return this.http.get<PanicAlert>(`${this.apiURL}/${id}`);
  }
}