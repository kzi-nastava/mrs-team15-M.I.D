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
}