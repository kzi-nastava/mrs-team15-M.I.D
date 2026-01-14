import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiURL = 'http://localhost:8081/api/users';
  private driverApiURL = 'http://localhost:8081/api/driver';

  constructor(private http: HttpClient) {}

  updateUser(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiURL}/${id}`, data, { observe: 'response' });
  }

  requestDriverChange(id: number, request: any): Observable<any> {
    return this.http.post(`${this.driverApiURL}/${id}/change-request`, request, { observe: 'response' });
  }

  getUser(id: number): Observable<any> {
    return this.http.get(`${this.apiURL}/${id}`);
  }
}
