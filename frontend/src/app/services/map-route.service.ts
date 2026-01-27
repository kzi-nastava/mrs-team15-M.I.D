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

  private markersSubject = new ReplaySubject<RouteData>(1);
  markers$ = this.markersSubject.asObservable();
  private vehicleLocationSubject = new BehaviorSubject<{ lat: number; lng: number } | null>(null);
  vehicleLocation$ = this.vehicleLocationSubject.asObservable();

  private centerOnVehicleSubject = new BehaviorSubject<boolean>(false);
  centerOnVehicle$ = this.centerOnVehicleSubject.asObservable();

  private currentRouteData: RouteData = { route: [], isAlert: false };

  drawRoute(route: any[], isAlert: boolean = false) {
    if (!route || route.length === 0) {
      console.warn('Empty route received');
      return;
    }
    this.currentRouteData = { route, isAlert };
    this.routeSubject.next(this.currentRouteData);
  }

  drawMarkers(points: any[], isAlert: boolean = false) {
    if (!points || points.length === 0) return;
    this.currentRouteData = { route: points, isAlert };
    this.markersSubject.next(this.currentRouteData);
  }

  alertRoute() {
    if (this.currentRouteData.route.length > 0) {
      this.currentRouteData.isAlert = true;
      this.routeSubject.next({ ...this.currentRouteData });
    } else {
      console.warn('No route to alert');
    }
  }

  clearAlert() {
    if (this.currentRouteData.route.length > 0) {
      this.currentRouteData.isAlert = false;
      this.routeSubject.next({ ...this.currentRouteData });
    }
  }

  updateVehicleLocation(lat: number, lng: number) {
    this.vehicleLocationSubject.next({ lat, lng });
  }

  updateVehicleLocationAndCenter(lat: number, lng: number) {
    this.vehicleLocationSubject.next({ lat, lng });
    this.centerOnVehicleSubject.next(true);
  }

  clearVehicleLocation() {
    this.vehicleLocationSubject.next(null);
  }

  clearRoute() {
    this.currentRouteData = { route: [], isAlert: false };
    this.routeSubject.next({ route: [], isAlert: false });
    this.markersSubject.next({ route: [], isAlert: false });
  }
}
