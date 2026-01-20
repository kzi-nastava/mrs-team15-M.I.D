import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Form } from '@angular/forms';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiURL = 'http://localhost:8081/api/users';
  private driverApiURL = 'http://localhost:8081/api/driver';

  constructor(private http: HttpClient) {}

  updateUser(data: FormData): Observable<any> {
    return this.http.put(`${this.apiURL}`, data, { observe: 'response' });
  }

  requestDriverChange(request: any): Observable<any> {
    return this.http.post(`${this.driverApiURL}/change-request`, request, { observe: 'response' });
  }

  getUser(): Observable<any> {
    return this.http.get(`${this.apiURL}`);
  }

  changePassword(id: number, data: { currentPassword: string; newPassword: string; confirmNewPassword: string; }): Observable<any> {
    return this.http.put(`${this.apiURL}/${id}/change-password`, data, { observe: 'response' });
  }

}
