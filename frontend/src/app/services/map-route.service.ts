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

  // Similar for markers, if we want to show them separately from the route
  private markersSubject = new ReplaySubject<RouteData>(1);
  markers$ = this.markersSubject.asObservable();
  private vehicleLocationSubject = new BehaviorSubject<{ lat: number; lng: number } | null>(null);
  vehicleLocation$ = this.vehicleLocationSubject.asObservable();

  // Subject to signal when the map should center on the vehicle's location (e.g., after receiving a new location update)
  private centerOnVehicleSubject = new BehaviorSubject<boolean>(false);
  centerOnVehicle$ = this.centerOnVehicleSubject.asObservable();

  private currentRouteData: RouteData = { route: [], isAlert: false };

  // Method to draw a route on the map, accepts an array of route points and an optional isAlert flag to indicate if the route should be highlighted as an alert, updates the current route data and emits it through the routeSubject for subscribers to consume
  drawRoute(route: any[], isAlert: boolean = false) {
    if (!route || route.length === 0) {
      console.warn('Empty route received');
      return;
    }
    this.currentRouteData = { route, isAlert };
    this.routeSubject.next(this.currentRouteData);
  }

  // Method to draw markers on the map, accepts an array of points and an optional isAlert flag to indicate if the markers should be highlighted as an alert, updates the current route data with the markers and emits it through the markersSubject for subscribers to consume
  drawMarkers(points: any[], isAlert: boolean = false) {
    if (!points || points.length === 0) return;
    this.currentRouteData = { route: points, isAlert };
    this.markersSubject.next(this.currentRouteData);
  }

  // Method to alert a route on the map, checks if there is a current route and if so, sets the isAlert flag to true and emits the updated route data through the routeSubject for subscribers to consume, if there is no route it logs a warning message
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
