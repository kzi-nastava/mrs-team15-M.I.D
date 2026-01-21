import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiURL = 'http://localhost:8081/api/admins';

  constructor(private http: HttpClient) {}

  registerDriver(data: any): Observable<any> {
    return this.http.post(`${this.apiURL}/driver-register`, data);
  }

  getDriverRequests(): Observable<any> {
    return this.http.get(`${this.apiURL}/driver-requests`);
  }

  getUserById(id: number): Observable<any> {
    return this.http.get(`${this.apiURL}/users/${id}`);
  }

  reviewDriverRequest(requestId: number | string, dto: any): Observable<any> {
    return this.http.put(`${this.apiURL}/driver-requests/${requestId}`, dto);
  }
}
