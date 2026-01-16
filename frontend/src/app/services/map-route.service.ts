import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })

export class MapRouteService {
  private routeSubject = new Subject<any[]>();
  route$ = this.routeSubject.asObservable();

  drawRoute(route: any[]) {
    if (!route || route.length === 0) {
      console.warn('Empty route received');
      return;
    }
    this.routeSubject.next(route);
  }
}
