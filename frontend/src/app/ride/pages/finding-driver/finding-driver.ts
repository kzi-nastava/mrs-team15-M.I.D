import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { MapComponent } from '../../../shared/components/map/map';
import { FindingDriverForm } from '../../components/finding-driver-form/finding-driver-form';
import { MapRouteService } from '../../../services/map-route.service';

@Component({
  selector: 'app-finding-driver',
  imports: [MapComponent, FindingDriverForm],
  templateUrl: './finding-driver.html',
  styleUrl: './finding-driver.css',
})
export class FindingDriver implements AfterViewInit {

  @ViewChild(MapComponent) private mapComponent?: MapComponent;

  // order data passed via navigation state
  order: any = null;

  constructor(private router: Router, private mapRouteService: MapRouteService) {
    // Try to read navigation state (works on direct navigate calls)
    try {
      const nav = this.router.getCurrentNavigation();
      this.order = nav && (nav.extras as any) && (nav.extras as any).state ? (nav.extras as any).state.order : null;
    } catch (e) {
      this.order = null;
    }
    // Fallback to history.state for cases where getCurrentNavigation is not available
    if (!this.order && typeof history !== 'undefined' && (history as any).state) {
      this.order = (history as any).state.order || null;
    }
    // Debug: log the incoming order/navigation state immediately (use console.log so it's visible)
    try {
      console.log('FindingDriver ctor - incoming order:', this.order);
      if (this.order) {
        try { console.log('order.route (ctor):', Array.isArray(this.order.route) ? `length=${this.order.route.length}` : this.order.route); } catch(e) {}
        try { console.log('order.routeLattitudes/Longitudes (ctor):', Array.isArray(this.order.routeLattitudes) ? `lats=${this.order.routeLattitudes.length}` : this.order.routeLattitudes, Array.isArray(this.order.routeLongitudes) ? `lngs=${this.order.routeLongitudes.length}` : this.order.routeLongitudes); } catch(e) {}
      }
    } catch (e) {}
  }

  ngAfterViewInit(): void {
    // Draw route after child map has subscribed to route updates
    try {
      if (this.order && Array.isArray(this.order.route) && this.order.route.length > 0) {
        try { console.log('FindingDriver ngAfterViewInit - drawing provided route, length=', this.order.route.length); console.log('route sample start/end', JSON.stringify(this.order.route[0]), JSON.stringify(this.order.route[this.order.route.length-1])); } catch(e) {}
        this.mapRouteService.drawRoute(this.order.route);
        // ensure map (which initializes async) receives the route; redraw shortly after
        try { setTimeout(() => { try { this.mapRouteService.drawRoute(this.order.route); } catch(e){} }, 250); } catch(e) {}
        // Also add start/end markers and separate stop markers (stops are separate from the route polyline)
        try { setTimeout(() => { try { this.addStartEndMarkersFromOrder(this.order.route); } catch(e){} }, 350); } catch(e) {}
        try { setTimeout(() => { try { this.addStopMarkersFromOrder(); } catch(e){} }, 450); } catch(e) {}
        return;
      }

      // If backend did not provide explicit route points but provided parallel arrays, rebuild points
      const lats: any = this.order && this.order.routeLattitudes;
      const lngs: any = this.order && this.order.routeLongitudes;
      if (Array.isArray(lats) && Array.isArray(lngs) && lats.length > 0 && lats.length === lngs.length) {
        try {
          const pts = lats.map((lat: number, i: number) => ({ lat: Number(lat), lng: Number(lngs[i]) }));
          try { console.log('FindingDriver ngAfterViewInit - rebuilding route from lat/lng arrays, count=', pts.length); console.log('pts sample', JSON.stringify(pts[0]), JSON.stringify(pts[pts.length-1])); } catch(e) {}
          this.mapRouteService.drawRoute(pts);
          try { setTimeout(() => { try { this.mapRouteService.drawRoute(pts); } catch(e){} }, 250); } catch(e) {}
          try { setTimeout(() => { try { this.addStartEndMarkersFromOrder(pts); } catch(e){} }, 350); } catch(e) {}
          try { setTimeout(() => { try { this.addStopMarkersFromOrder(); } catch(e){} }, 450); } catch(e) {}
          return;
        } catch (e) {
          console.warn('rebuilding route from lat/lng arrays failed', e);
        }
      }

      // Final fallback: use start/stop/end geo coordinates (markers-only if no polyline available)
      try {
        const pts: any[] = [];
        if (this.order && this.order.startLatitude != null && this.order.startLongitude != null) {
          pts.push({ lat: this.order.startLatitude, lng: this.order.startLongitude, display: this.order.startAddress });
        }
        if (this.order && Array.isArray(this.order.stopLatitudes) && Array.isArray(this.order.stopLongitudes)) {
          for (let i = 0; i < Math.min(this.order.stopLatitudes.length, this.order.stopLongitudes.length); i++) {
            pts.push({ lat: this.order.stopLatitudes[i], lng: this.order.stopLongitudes[i], display: (this.order.stopAddresses && this.order.stopAddresses[i]) || undefined });
          }
        }
        if (this.order && this.order.endLatitude != null && this.order.endLongitude != null) {
          pts.push({ lat: this.order.endLatitude, lng: this.order.endLongitude, display: this.order.endAddress });
        }
        if (pts.length > 0) {
          try { console.log('FindingDriver ngAfterViewInit - fallback pts count=', pts.length); console.log('pts sample', JSON.stringify(pts[0]), JSON.stringify(pts[pts.length-1])); } catch(e) {}
          // prefer polyline when more than one point
          if (pts.length > 1) {
            this.mapRouteService.drawRoute(pts);
            try { setTimeout(() => { try { this.mapRouteService.drawRoute(pts); } catch(e){} }, 250); } catch(e) {}
            try { setTimeout(() => { try { this.addStartEndMarkersFromOrder(pts); } catch(e){} }, 350); } catch(e) {}
            try { setTimeout(() => { try { this.addStopMarkersFromOrder(); } catch(e){} }, 450); } catch(e) {}
          } else {
            this.mapRouteService.drawMarkers(pts);
            try { setTimeout(() => { try { this.mapRouteService.drawMarkers(pts); } catch(e){} }, 250); } catch(e) {}
          }
        }
      } catch (e) {
        console.warn('fallback drawing for order route failed', e);
      }
    } catch (e) {
      console.warn('drawing passed-in route failed', e);
    }
  }

  private async addMarkersDirectly(route: any[]) {
    try {
      const mapComp: any = this.mapComponent as any;
      if (!mapComp) return;
      const map = mapComp.map || (mapComp as any)._map || (mapComp as any)['_map'];
      const L = (window as any).L || await import('leaflet');
      if (!map) {
        console.warn('addMarkersDirectly: map instance not available on MapComponent');
        return;
      }

      // add start marker
      try {
        if (route.length > 0) {
          const s = route[0];
          L.circleMarker([s.lat, s.lng], { radius: 8, color: '#22c55e', fillColor: '#22c55e', fillOpacity: 0.6 }).addTo(map).bindPopup(s.display || s.name || 'Start');
        }
      } catch (e) { console.warn('addMarkersDirectly start failed', e); }

      // add end marker
      try {
        if (route.length > 1) {
          const e = route[route.length - 1];
          L.circleMarker([e.lat, e.lng], { radius: 8, color: '#ef4444', fillColor: '#ef4444', fillOpacity: 0.6 }).addTo(map).bindPopup(e.display || e.name || 'End');
        }
      } catch (e) { console.warn('addMarkersDirectly end failed', e); }

      // add intermediate markers
      try {
        const intermediate = route.slice(1, Math.max(1, route.length - 1));
        intermediate.forEach((p: any) => {
          try {
            L.circleMarker([p.lat, p.lng], { radius: 6, color: '#2563eb', fillColor: '#2563eb', fillOpacity: 0.85 }).addTo(map).bindPopup(p.display || p.name || 'Stop');
          } catch (inner) { console.warn('addMarkersDirectly intermediate failed', inner); }
        });
      } catch (e) { console.warn('addMarkersDirectly intermediate block failed', e); }
    } catch (err) {
      console.warn('addMarkersDirectly failed', err);
    }
  }

  // Add start/end markers using explicit order start/end coordinates if present,
  // otherwise fall back to the provided route endpoints.
  private async addStartEndMarkersFromOrder(route?: any[]) {
    try {
      const mapComp: any = this.mapComponent as any;
      if (!mapComp) return;
      const map = mapComp.map || (mapComp as any)._map || (mapComp as any)['_map'];
      const L = (window as any).L || await import('leaflet');
      if (!map) return;

      // start
      try {
        if (this.order && this.order.startLatitude != null && this.order.startLongitude != null) {
          const sLat = Number(this.order.startLatitude);
          const sLng = Number(this.order.startLongitude);
          L.circleMarker([sLat, sLng], { radius: 8, color: '#22c55e', fillColor: '#22c55e', fillOpacity: 0.6 }).addTo(map).bindPopup(this.order.startAddress || 'Start');
        } else if (route && route.length > 0) {
          const s = route[0];
          L.circleMarker([s.lat, s.lng], { radius: 8, color: '#22c55e', fillColor: '#22c55e', fillOpacity: 0.6 }).addTo(map).bindPopup(s.display || s.name || 'Start');
        }
      } catch (e) { console.warn('addStartEndMarkersFromOrder start failed', e); }

      // end
      try {
        if (this.order && this.order.endLatitude != null && this.order.endLongitude != null) {
          const eLat = Number(this.order.endLatitude);
          const eLng = Number(this.order.endLongitude);
          L.circleMarker([eLat, eLng], { radius: 8, color: '#ef4444', fillColor: '#ef4444', fillOpacity: 0.6 }).addTo(map).bindPopup(this.order.endAddress || 'End');
        } else if (route && route.length > 1) {
          const e = route[route.length - 1];
          L.circleMarker([e.lat, e.lng], { radius: 8, color: '#ef4444', fillColor: '#ef4444', fillOpacity: 0.6 }).addTo(map).bindPopup(e.display || e.name || 'End');
        }
      } catch (e) { console.warn('addStartEndMarkersFromOrder end failed', e); }
    } catch (err) {
      console.warn('addStartEndMarkersFromOrder failed', err);
    }
  }

  // Add stop markers from explicit order stop arrays (stopLatitudes/stopLongitudes/stopAddresses).
  private async addStopMarkersFromOrder() {
    try {
      if (!this.order) return;
      const lats = this.order.stopLatitudes;
      const lngs = this.order.stopLongitudes;
      const addrs = this.order.stopAddresses;
      if (!Array.isArray(lats) || !Array.isArray(lngs) || lats.length === 0 || lats.length !== lngs.length) return;

      const mapComp: any = this.mapComponent as any;
      if (!mapComp) return;
      const map = mapComp.map || (mapComp as any)._map || (mapComp as any)['_map'];
      const L = (window as any).L || await import('leaflet');
      if (!map) return;

      for (let i = 0; i < lats.length; i++) {
        try {
          const lat = Number(lats[i]);
          const lng = Number(lngs[i]);
          const display = Array.isArray(addrs) && addrs.length === lats.length ? addrs[i] : (addrs && addrs[i]) || 'Stop';
          L.circleMarker([lat, lng], { radius: 6, color: '#2563eb', fillColor: '#2563eb', fillOpacity: 0.85 }).addTo(map).bindPopup(display);
        } catch (inner) { console.warn('addStopMarkersFromOrder marker failed', inner); }
      }
    } catch (err) {
      console.warn('addStopMarkersFromOrder failed', err);
    }
  }

}
