import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiURL = 'http://localhost:8081/api/users';

  constructor(private http: HttpClient) {}

  updateUser(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiURL}/${id}`, data, { observe: 'response' });
  }

  getUser(id: number): Observable<any> {
    return this.http.get(`${this.apiURL}/${id}`);
  }
}
