import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

export interface RouteData {
  route: any[];
  isAlert?: boolean;
}

@Injectable({ providedIn: 'root' })
export class MapRouteService {
  private routeSubject = new Subject<RouteData>();
  route$ = this.routeSubject.asObservable();

  private isAlertMode = new BehaviorSubject<boolean>(false);
  isAlert$ = this.isAlertMode.asObservable();

  private vehicleLocationSubject = new BehaviorSubject<{ lat: number; lng: number } | null>(null);
  vehicleLocation$ = this.vehicleLocationSubject.asObservable();

  drawRoute(route: any[], isAlert: boolean = false) {
    if (!route || route.length === 0) {
      console.warn('Empty route received');
      return;
    }
    this.routeSubject.next({ route, isAlert });
    this.isAlertMode.next(isAlert);
  }

  alertRoute() {
    this.isAlertMode.next(true);
  }

  clearAlert() {
    this.isAlertMode.next(false);
  }

  updateVehicleLocation(lat: number, lng: number) {
    this.vehicleLocationSubject.next({ lat, lng });
  }

  clearVehicleLocation() {
    this.vehicleLocationSubject.next(null);
  }
}
