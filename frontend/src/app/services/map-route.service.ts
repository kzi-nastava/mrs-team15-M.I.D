import { Injectable } from '@angular/core';
import { BehaviorSubject, ReplaySubject } from 'rxjs';

export interface RouteData {
  route: any[];
  isAlert?: boolean;
}

@Injectable({ providedIn: 'root' })
export class MapRouteService {
  // ReplaySubject(1) buffers the latest route so components that subscribe
  // after a route was emitted (e.g., map recreated on reload) still receive it.
  private routeSubject = new ReplaySubject<RouteData>(1);
  route$ = this.routeSubject.asObservable();

  private isAlertMode = new BehaviorSubject<boolean>(false);
  isAlert$ = this.isAlertMode.asObservable();

  // Separate subject for marker-only displays; buffer latest as well
  private markersSubject = new ReplaySubject<RouteData>(1);
  markers$ = this.markersSubject.asObservable();
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

  drawMarkers(points: any[], isAlert: boolean = false) {
    if (!points || points.length === 0) return;
    this.markersSubject.next({ route: points, isAlert });
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
