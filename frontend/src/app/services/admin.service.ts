import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiURL = 'http://localhost:8081/api/admins';

  constructor(private http: HttpClient) {}

  registerDriver(data: FormData): Observable<any> {
    return this.http.post(`${this.apiURL}/driver-register`, data);
  }

  getDriverRequests(): Observable<any> {
    return this.http.get(`${this.apiURL}/driver-requests`);
  }

  getUserById(id: number): Observable<any> {
    return this.http.get(`${this.apiURL}/users/${id}`);
  }

  getAllUsers(search?: string, sortBy?: string, sortDirection?: string, page?: number, size?: number): Observable<any> {
    const params: any = {};
    if (search) params.search = search;
    if (sortBy) params.sortBy = sortBy;
    if (sortDirection) params.sortDirection = sortDirection;
    if (page != null) params.page = String(page);
    if (size != null) params.size = String(size);
    return this.http.get(`${this.apiURL}/users`, { params });
  }

  reviewDriverRequest(requestId: number | string, dto: any): Observable<any> {
    return this.http.put(`${this.apiURL}/driver-requests/${requestId}`, dto);
  }

  blockUser(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.apiURL}/block/${id}`, { reason });
  }

  unblockUser(id: number): Observable<any> {
    return this.http.put(`${this.apiURL}/unblock/${id}`, null);

  }

    getPriceConfigurations(): Observable<any> {
    return this.http.get(`${this.apiURL}/price-configs`);
  }

  updatePriceConfigurations(priceData: any): Observable<any> {
    return this.http.put(`${this.apiURL}/price-configs`, priceData);
  }

  getActiveRides(): Observable<any> {
    console.log('AdminService.getActiveRides() called');
    const activeRidesUrl = 'http://localhost:8081/api/rides/active-rides';
    console.log('GET request URL:', activeRidesUrl);
    return this.http.get(activeRidesUrl);
  }

  // Calls backend GET /report with query parameters.
  getReport(params: { startDate?: number | null; endDate?: number | null; drivers?: boolean; users?: boolean; personId?: string | number | null }): Observable<any> {
    const url = `${this.apiURL}/report`;
    const httpParams: any = {};
    if (params.startDate != null) httpParams.startDate = String(params.startDate);
    if (params.endDate != null) httpParams.endDate = String(params.endDate);
    if (params.drivers != null) httpParams.drivers = String(params.drivers);
    if (params.users != null) httpParams.users = String(params.users);
    if (params.personId != null) httpParams.personId = String(params.personId);
    console.log('AdminService.getReport() called, URL:', url, 'params:', httpParams);
    return this.http.get(url, { params: httpParams });
  }
}
