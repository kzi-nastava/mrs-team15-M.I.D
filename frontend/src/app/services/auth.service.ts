import { Observable } from "rxjs";
import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

interface ActivateResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiURL = "http://localhost:8080/api/auth";

    constructor(private http : HttpClient){}
    
    register(data : FormData) : Observable<any> {
      return this.http.post(`${this.apiURL}/register`, data);
    } 

    login(data: { email: string; password: string; }) : Observable<any> {
      return this.http.post<{ token: string }>(`${this.apiURL}/login`, data);
    }

    activate(token: string): Observable<ActivateResponse> {
      return this.http.put<ActivateResponse>(`${this.apiURL}/activate?token=${token}`, null);
    }
}