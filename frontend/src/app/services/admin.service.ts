import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiURL = 'http://localhost:8081/api/admins';

  constructor(private http: HttpClient) {}

  registerDriver(adminId: number, data: any): Observable<any> {
    return this.http.post(`${this.apiURL}/${adminId}/driver-register`, data);
  }

  getDriverRequests(adminId: number): Observable<any> {
    return this.http.get(`${this.apiURL}/${adminId}/driver-requests`);
  }

  reviewDriverRequest(adminId: number, requestId: number | string, dto: any): Observable<any> {
    return this.http.put(`${this.apiURL}/${adminId}/driver-requests/${requestId}`, dto);
  }
}
