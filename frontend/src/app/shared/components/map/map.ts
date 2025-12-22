import { AfterViewInit, Component, Input, OnDestroy, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { VehicleService } from '../../../services/vehicle.service';
import { Subscription } from 'rxjs';
import { Vehicle } from '../../../model/vehicle.model';

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
  private vehicleMarkers: Map<number, any> = new Map();
  private vehiclesSubscription?: Subscription;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private vehicleService: VehicleService
  ) {}

  async ngAfterViewInit(): Promise<void> {
    if (isPlatformBrowser(this.platformId)) {
      await this.initMap();
    }
  }

  ngOnDestroy(): void {
    if (this.vehiclesSubscription) {
      this.vehiclesSubscription.unsubscribe();
    }
    this.vehicleService.stopVehicleMovement();
  }

  private async initMap(): Promise<void> {
    const L = await import('leaflet');

    // Initialize the map
    this.map = L.map('map').setView([this.centerLat, this.centerLng], this.zoom);

    // Set up the OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    if (this.showVehicles) {
      // Subscribe to vehicle updates
      this.vehiclesSubscription = this.vehicleService.vehicles$.subscribe(vehicles => {
        this.updateVehicleMarkers(vehicles, L);
      });

      // Get user's location and initialize vehicles
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            this.map.setView([lat, lng], this.zoom);
            this.vehicleService.initializeVehicles(lat, lng, 10);
          },
          (error) => {
            console.error('Error getting location:', error);
            this.vehicleService.initializeVehicles(this.centerLat, this.centerLng, 10);
          }
        );
      } else {
        console.warn('Geolocation not supported');
        this.vehicleService.initializeVehicles(this.centerLat, this.centerLng, 10);
      }
    }
  }

  private updateVehicleMarkers(vehicles: Vehicle[], L: any): void {
    vehicles.forEach(vehicle => {
      let marker = this.vehicleMarkers.get(vehicle.id);

      if (!marker) {
        marker = this.createVehicleMarker(vehicle, L);
        this.vehicleMarkers.set(vehicle.id, marker);
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
      .bindPopup(`Car #${vehicle.id}<br>${vehicle.available ? 'Available' : 'In use'}`);
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
}
