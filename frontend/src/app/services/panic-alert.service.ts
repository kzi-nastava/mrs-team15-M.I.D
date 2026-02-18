import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PanicAlert } from './notification-websocket.service';
import { environment } from '../../environments/environment';

export interface PanicAlertResponse {
  content: PanicAlert[];
  pageable: {
    pageNumber: number;
    pageSize: number;
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
  empty: boolean;
}


@Injectable({
  providedIn: 'root'
})
export class PanicAlertService {
  private apiURL = environment.apiUrl + '/panic-alerts';
  constructor(private http: HttpClient) {}

  getAllAlerts(includeResolved: boolean = false): Observable<PanicAlert[]> {
    return this.http.get<PanicAlert[]>(`${this.apiURL}/all`, {
      params: { includeResolved: includeResolved.toString() }
    });
  }

resolvePanicAlert(id: number): Observable<string> {
  return this.http.put(`${this.apiURL}/${id}/resolve`, {}, { responseType: 'text' });
}

  getAlertById(id: number): Observable<PanicAlert> {
    return this.http.get<PanicAlert>(`${this.apiURL}/${id}`);
  }

  getUnresolvedAlerts(page: number, size: number): Observable<PanicAlertResponse> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PanicAlertResponse>(`${this.apiURL}/unresolved`, { params });
  }
}