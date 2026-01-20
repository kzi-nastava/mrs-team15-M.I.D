import { AfterViewInit, Component, Input, OnDestroy, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { VehicleService } from '../../../services/vehicle.service';
import { Subscription } from 'rxjs';
import { Vehicle } from '../../../model/vehicle.model';
import { MapRouteService } from '../../../services/map-route.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [],
  templateUrl: './map.html',
  styleUrl: './map.css'
})
export class MapComponent implements AfterViewInit, OnDestroy {
  @Input() centerLat: number = 45.2552;
  @Input() centerLng: number = 19.8452;
  @Input() zoom: number = 13;
  @Input() showVehicles: boolean = true;

  private map: any;
  private vehicleMarkers: Map<string, any> = new Map();
  private vehiclesSubscription?: Subscription;
  private routeSubscription?: Subscription;
  private alertSubscription?: Subscription;

  private currentRoute: any[] = [];
  private isAlertMode: boolean = false;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private vehicleService: VehicleService,
    private mapRouteService: MapRouteService
  ) {}

  async ngAfterViewInit(): Promise<void> {
    if (isPlatformBrowser(this.platformId)) {
      await this.initMap();
    }

    this.routeSubscription = this.mapRouteService.route$.subscribe(routeData => {
      this.currentRoute = routeData.route;
      this.isAlertMode = routeData.isAlert || false;
      this.drawRoute(routeData.route, routeData.isAlert);
    });

  this.alertSubscription = this.mapRouteService.isAlert$.subscribe(isAlert => {
    this.isAlertMode = isAlert;
    if (this.currentRoute.length > 0) {
      this.drawRoute(this.currentRoute, isAlert);
    } else {
      console.warn('No route to alert!');
    }
  });
}

  ngOnDestroy(): void {
    if (this.vehiclesSubscription) {
      this.vehiclesSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    if (this.alertSubscription) {
      this.alertSubscription.unsubscribe();
    }
  }

  private async initMap(): Promise<void> {
    const L = await import('leaflet');
    this.map = L.map('map').setView([this.centerLat, this.centerLng], this.zoom);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    if (this.showVehicles) {
      this.vehiclesSubscription = this.vehicleService.vehicles$.subscribe(vehicles => {
        this.updateVehicleMarkers(vehicles, L);
      });

      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            this.map.setView([lat, lng], this.zoom);
            this.vehicleService.initializeVehicles(lat, lng);
          },
          (error) => {
            console.error('Error getting location:', error);
            this.vehicleService.initializeVehicles(this.centerLat, this.centerLng);
          }
        );
      } else {
        console.warn('Geolocation not supported');
        this.vehicleService.initializeVehicles(this.centerLat, this.centerLng);
      }
    }
  }

  private updateVehicleMarkers(vehicles: Vehicle[], L: any): void {
    vehicles.forEach(vehicle => {
      let marker = this.vehicleMarkers.get(vehicle.licencePlate);

      if (!marker) {
        marker = this.createVehicleMarker(vehicle, L);
        this.vehicleMarkers.set(vehicle.licencePlate, marker);
      } else {
        marker.setLatLng([vehicle.lat, vehicle.lng]);

        const iconUrl = this.getVehicleIcon(vehicle.available);
        const icon = L.icon({
          iconUrl: iconUrl,
          iconSize: [30, 30],
          iconAnchor: [15, 15],
          popupAnchor: [0, -15]
        });
        marker.setIcon(icon);
      }
    });
  }

  private createVehicleMarker(vehicle: Vehicle, L: any): any {
    const iconUrl = this.getVehicleIcon(vehicle.available);
    const icon = L.icon({
      iconUrl: iconUrl,
      iconSize: [30, 30],
      iconAnchor: [15, 15],
      popupAnchor: [0, -15]
    });

    return L.marker([vehicle.lat, vehicle.lng], { icon })
      .addTo(this.map)
      .bindPopup(`${vehicle.licencePlate}<br>${vehicle.available ? 'Available' : 'In use'}`);
  }

  private getVehicleIcon(available: boolean): string {
    const color = available ? '#22c55e' : '#ef4444';

    const svgString = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
        <circle cx="16" cy="16" r="14" fill="${color}" stroke="white" stroke-width="2"/>
        <circle cx="12" cy="20" r="2.5" fill="white"/>
        <circle cx="20" cy="20" r="2.5" fill="white"/>
        <rect x="8" y="16" width="16" height="4" fill="white"/>
        <rect x="12" y="13" width="11" height="3" fill="white"/>
      </svg>
    `;

    return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svgString);
  }

  private routeLayer?: any;
  private startMarker?: any;
  private endMarker?: any;

  private async drawRoute(route: any[], isAlert: boolean = false) {
    if (!this.map || !route || route.length === 0) {
      console.warn('drawRoute called with no route');
      return;
    }

  this.currentRoute = route;
  this.isAlertMode = isAlert;

  const L = await import('leaflet');
  const latLngs: [number, number][] = route.map(p => [p.lat, p.lng]);

  if (this.routeLayer) this.map.removeLayer(this.routeLayer);
  if (this.startMarker) this.map.removeLayer(this.startMarker);
  if (this.endMarker) this.map.removeLayer(this.endMarker);

  const routeColor = isAlert ? "#ef4444" : "#111";
  const routeWeight = isAlert ? 6 : 5;

  this.routeLayer = L.polyline(latLngs, {
    weight: routeWeight,
    color: routeColor,
    opacity: isAlert ? 0.9 : 0.8,
    lineCap: "round",
    lineJoin: "round",
    className: isAlert ? 'alert-route' : ''
  }).addTo(this.map);

  this.startMarker = L.circleMarker(latLngs[0], {
    radius: isAlert ? 10 : 8,
    color: "#22c55e",
    fillColor: "#22c55e",
    fillOpacity: 0.4
  }).addTo(this.map);

  this.endMarker = L.circleMarker(latLngs[latLngs.length - 1], {
    radius: isAlert ? 10 : 8,
    color: isAlert ? "#dc2626" : "#ef4444",
    fillColor: isAlert ? "#dc2626" : "#ef4444",
    fillOpacity: isAlert ? 0.6 : 0.4
  }).addTo(this.map);

  if (isAlert) {
    const center = this.map.getCenter();
  }

  this.map.fitBounds(this.routeLayer.getBounds(), {
    padding: [30, 30]
  });
}

  private async drawRoutee(route: any[], isAlert: boolean = false) {
    if (!this.map || !route || route.length === 0) return;

    const L = await import('leaflet');

    const latLngs: [number, number][] = route.map(p => [p.lat, p.lng]);

    if (this.routeLayer) this.map.removeLayer(this.routeLayer);
    if (this.startMarker) this.map.removeLayer(this.startMarker);
    if (this.endMarker) this.map.removeLayer(this.endMarker);

    const routeColor = isAlert ? "#ef4444" : "#111";
    const routeWeight = isAlert ? 6 : 5;

    this.routeLayer = L.polyline(latLngs, {
      weight: routeWeight,
      color: routeColor,
      opacity: isAlert ? 0.9 : 0.8,
      lineCap: "round",
      lineJoin: "round",
      className: isAlert ? 'alert-route' : ''
    }).addTo(this.map);

    this.startMarker = L.circleMarker(latLngs[0], {
      radius: isAlert ? 10 : 8,
      color: "#22c55e",
      fillColor: "#22c55e",
      fillOpacity: 0.4
    }).addTo(this.map);

    this.endMarker = L.circleMarker(latLngs[latLngs.length - 1], {
      radius: isAlert ? 10 : 8,
      color: isAlert ? "#dc2626" : "#ef4444",
      fillColor: isAlert ? "#dc2626" : "#ef4444",
      fillOpacity: isAlert ? 0.6 : 0.4
    }).addTo(this.map);

    if (isAlert) {
      const center = this.map.getCenter();
    }

    this.map.fitBounds(this.routeLayer.getBounds(), {
      padding: [30, 30]
    });
  }
}
