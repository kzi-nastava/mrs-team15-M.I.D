import { AfterViewInit, Component, Input, OnDestroy, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { VehicleService } from '../../../services/vehicle.service';
import { Subscription } from 'rxjs';
import { Vehicle } from '../../../model/vehicle.model';
import { MapRouteService } from '../../../services/map-route.service';
import { LocationTrackingService } from '../../../services/location-tracking.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [],
  templateUrl: './map.html',
  styleUrls: ['./map.css']
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
  private markersSubscription?: Subscription;
  private vehicleLocationSubscription?: Subscription;
  private driverLocationSubscription?: Subscription;
  private trackedVehicleMarker?: any;
  private driverLocationMarker?: any;
  private driverLicencePlate: string | null = null;

  private routeLayer?: any;
  private startMarker?: any;
  private endMarker?: any;
  private markersLayerGroup?: any;
  
  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private vehicleService: VehicleService,
    private mapRouteService: MapRouteService,
    private locationTrackingService: LocationTrackingService
  ) {}

  async ngAfterViewInit(): Promise<void> {
    if (isPlatformBrowser(this.platformId)) {
      await this.initMap();
    }
    
    // Simple subscription - just redraw when route data changes
    this.routeSubscription = this.mapRouteService.route$.subscribe(routeData => {
      console.log('Route subscription fired:', routeData);
      if (!routeData.route || routeData.route.length === 0) {
        this.clearAllLayers();
        return;
      }
      
      this.drawRoute(routeData.route, routeData.isAlert || false);
    });
    
    this.markersSubscription = this.mapRouteService.markers$.subscribe(routeData => {
      console.log('Markers subscription fired:', routeData);
      this.drawMarkers(routeData.route, routeData.isAlert || false);
    });
    
    this.vehicleLocationSubscription = this.mapRouteService.vehicleLocation$.subscribe(async location => {
      if (location) {
        const L = await import('leaflet');
        this.updateTrackedVehicle(location.lat, location.lng, L);
      } else {
        this.clearTrackedVehicle();
      }
    });

    this.driverLocationSubscription = this.locationTrackingService.currentLocation$.subscribe(async location => {
      if (location) {
        const L = await import('leaflet');
        this.updateDriverLocation(location.lat, location.lon, L);
      } else {
        this.clearDriverLocation();
      }
    });

    this.locationTrackingService.driverLicencePlate$.subscribe(licencePlate => {
      this.driverLicencePlate = licencePlate;
    });
  }

  ngOnDestroy(): void {
    if (this.vehicleLocationSubscription) {
      this.vehicleLocationSubscription.unsubscribe();
    }
    if (this.driverLocationSubscription) {
      this.driverLocationSubscription.unsubscribe();
    }
    if (this.vehiclesSubscription) {
      this.vehiclesSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    if (this.markersSubscription) {
      this.markersSubscription.unsubscribe();
    }
    this.vehicleService.stopFetchingVehicles();
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
    const filteredVehicles = this.driverLicencePlate
      ? vehicles.filter(v => v.licencePlate !== this.driverLicencePlate)
      : vehicles;

    this.vehicleMarkers.forEach((marker, licencePlate) => {
      if (!filteredVehicles.find(v => v.licencePlate === licencePlate)) {
        this.map.removeLayer(marker);
        this.vehicleMarkers.delete(licencePlate);
      }
    });

    filteredVehicles.forEach(vehicle => {
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

  private async drawRoute(route: any[], isAlert: boolean = false) {
    if (!this.map || !route?.length) return;

    console.log('Drawing route, isAlert:', isAlert);

    // Clear existing layers
    this.clearRouteLayers();

    const L = await import('leaflet');
    const latLngs: [number, number][] = route.map(p => [p.lat, p.lng]);

    // Draw the route with color based on alert status
    this.routeLayer = L.polyline(latLngs, {
      color: isAlert ? "#ef4444" : "#111",
      weight: isAlert ? 6 : 5,
      opacity: isAlert ? 0.9 : 0.8,
      lineCap: "round",
      lineJoin: "round"
    }).addTo(this.map);

    console.log('Route drawn with color:', isAlert ? "#ef4444" : "#111");

    // Start marker (green)
    if (route.length > 0) {
      const start = route[0];
      this.startMarker = L.circleMarker([start.lat, start.lng], {
        radius: 8,
        color: '#22c55e',
        fillColor: '#22c55e',
        fillOpacity: 0.6
      }).bindPopup(start.display || start.name || 'Pickup address').addTo(this.map);
    }

    // End marker (red, darker if alert)
    if (route.length > 1) {
      const end = route[route.length - 1];
      this.endMarker = L.circleMarker([end.lat, end.lng], {
        radius: 8,
        color: isAlert ? '#dc2626' : '#ef4444',
        fillColor: isAlert ? '#dc2626' : '#ef4444',
        fillOpacity: 0.6
      }).bindPopup(end.display || end.name || 'Destination address').addTo(this.map);
    }

    this.map.fitBounds(this.routeLayer.getBounds(), { padding: [30, 30] });
  }

  private clearRouteLayers(): void {
    if (!this.map) return;
    
    if (this.routeLayer) {
      this.map.removeLayer(this.routeLayer);
      this.routeLayer = undefined;
    }
    if (this.startMarker) {
      this.map.removeLayer(this.startMarker);
      this.startMarker = undefined;
    }
    if (this.endMarker) {
      this.map.removeLayer(this.endMarker);
      this.endMarker = undefined;
    }
    if (this.markersLayerGroup) {
      this.map.removeLayer(this.markersLayerGroup);
      this.markersLayerGroup = undefined;
    }
  }

  private clearAllLayers(): void {
    this.clearRouteLayers();
    
    // Remove any stray polylines
    try {
      const globalL = (window as any).L;
      if (this.map && globalL) {
        this.map.eachLayer((layer: any) => {
          try {
            if (layer instanceof globalL.Polyline) {
              this.map.removeLayer(layer);
            }
          } catch (inner) {}
        });
      }
    } catch (e) {
      console.debug('clearAllLayers: failed to remove extra polylines', e);
    }
  }

  private async drawMarkers(route: any[], isAlert: boolean = false) {
    if (!this.map || !route || route.length === 0) return;
    const L = (window as any).L || await import('leaflet');

    this.clearRouteLayers();
    await new Promise(resolve => setTimeout(resolve, 50));

    // start marker
    if (route.length > 0) {
      const start = route[0];
      this.startMarker = L.circleMarker([start.lat, start.lng], {
        radius: 8,
        color: '#22c55e',
        fillColor: '#22c55e',
        fillOpacity: 0.6
      }).bindPopup(start.display || start.name || 'Start').addTo(this.map);
    }
    
    // end marker
    if (route.length > 1) {
      const end = route[route.length - 1];
      this.endMarker = L.circleMarker([end.lat, end.lng], {
        radius: 8,
        color: isAlert ? '#dc2626' : '#ef4444',
        fillColor: isAlert ? '#dc2626' : '#ef4444',
        fillOpacity: 0.6
      }).bindPopup(end.display || end.name || 'End').addTo(this.map);
    }
    
    // intermediate markers
    try {
      const intermediate = route.slice(1, Math.max(1, route.length - 1));
      if (intermediate && intermediate.length > 0) {
        this.markersLayerGroup = L.layerGroup();
        intermediate.forEach((p: any) => {
          try {
            const m = L.circleMarker([p.lat, p.lng], {
              radius: 6,
              color: '#2563eb',
              fillColor: '#2563eb',
              fillOpacity: 0.85
            }).bindPopup(p.display || p.name || 'Stop');
            this.markersLayerGroup.addLayer(m);
          } catch (inner) { 
            console.warn('failed to add intermediate marker', inner); 
          }
        });
        if (this.markersLayerGroup.getLayers().length > 0) {
          this.markersLayerGroup.addTo(this.map);
        }
      }
    } catch (e) {
      console.warn('adding intermediate markers failed', e);
    }
    
    // fit bounds
    try {
      const groups: any[] = [];
      if (this.startMarker) groups.push(this.startMarker);
      if (this.endMarker) groups.push(this.endMarker);
      if (this.markersLayerGroup && this.markersLayerGroup.getLayers().length > 0) {
        groups.push(...this.markersLayerGroup.getLayers());
      }
      if (groups.length > 1) {
        const group = L.featureGroup(groups);
        this.map.fitBounds(group.getBounds(), { padding: [30, 30] });
      } else if (this.startMarker) {
        this.map.setView(this.startMarker.getLatLng(), this.zoom);
      }
    } catch (e) {
      console.warn('fitBounds markers failed', e);
    }
  }

  private async updateTrackedVehicle(lat: number, lng: number, L: any): Promise<void> {
    if (!this.map) return;

    if (this.trackedVehicleMarker) {
      this.trackedVehicleMarker.setLatLng([lat, lng]);
    } else {
      const svgString = `
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
          <circle cx="16" cy="16" r="14" fill="#3b82f6" stroke="white" stroke-width="3"/>
          <circle cx="12" cy="20" r="2.5" fill="white"/>
          <circle cx="20" cy="20" r="2.5" fill="white"/>
          <rect x="8" y="16" width="16" height="4" fill="white"/>
          <rect x="12" y="13" width="11" height="3" fill="white"/>
          <circle cx="16" cy="16" r="4" fill="yellow" opacity="0.8"/>
        </svg>
      `;

      const icon = L.icon({
        iconUrl: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svgString),
        iconSize: [40, 40],
        iconAnchor: [20, 20],
        popupAnchor: [0, -20]
      });

      this.trackedVehicleMarker = L.marker([lat, lng], { icon })
        .addTo(this.map)
        .bindPopup('Your ride vehicle');
    }
  }

  private clearTrackedVehicle(): void {
    if (this.trackedVehicleMarker && this.map) {
      this.map.removeLayer(this.trackedVehicleMarker);
      this.trackedVehicleMarker = undefined;
    }
  }

  private updateDriverLocation(lat: number, lon: number, L: any): void {
    if (!this.map) return;

    if (this.driverLocationMarker) {
      this.driverLocationMarker.setLatLng([lat, lon]);
    } else {
      const svgString = `
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
          <circle cx="16" cy="16" r="14" fill="#10b981" stroke="white" stroke-width="3"/>
          <circle cx="16" cy="16" r="6" fill="white"/>
          <circle cx="16" cy="16" r="3" fill="#10b981"/>
        </svg>
      `;

      const icon = L.icon({
        iconUrl: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svgString),
        iconSize: [36, 36],
        iconAnchor: [18, 18],
        popupAnchor: [0, -18]
      });

      this.driverLocationMarker = L.marker([lat, lon], { icon })
        .addTo(this.map)
        .bindPopup('Your current location');
    }
  }

  private clearDriverLocation(): void {
    if (this.driverLocationMarker && this.map) {
      this.map.removeLayer(this.driverLocationMarker);
      this.driverLocationMarker = undefined;
    }
  }
}