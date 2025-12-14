import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
import { Vehicle } from '../../model/vehicle.model';
import { VehicleService } from '../../services/vehicle.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing implements AfterViewInit, OnDestroy {
  private map!: L.Map;
  private vehicleMarkers: Map<number, L.Marker> = new Map();
  private vehiclesSubscription?: Subscription;

  constructor(private vehicleService: VehicleService) {}

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    if (this.vehiclesSubscription) {
      this.vehiclesSubscription.unsubscribe();
    }
    this.vehicleService.stopVehicleMovement();
  }

  private initMap(): void {
    // Initialize the map
    this.map = L.map('map').setView([45.2552, 19.8452], 13);

    // Set up the OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Subscribe to vehicle updates
    this.vehiclesSubscription = this.vehicleService.vehicles$.subscribe(vehicles => {
      this.updateVehicleMarkers(vehicles);
    });

    // Get user's location
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;

          // Center map on user location
          this.map.setView([lat, lng], 13);

          // Initialize vehicles around user location
          this.vehicleService.initializeVehicles(lat, lng, 10);
        },
        (error) => {
          console.error('Error getting location:', error);
          // Use default location
          this.vehicleService.initializeVehicles(45.2552, 19.8452, 10);
        }
      );
    } else {
      // Browser doesn't support geolocation
      console.warn('Geolocation not supported');
      this.vehicleService.initializeVehicles(45.2552, 19.8452, 10);
    }
  }

  private updateVehicleMarkers(vehicles: Vehicle[]): void {
    vehicles.forEach(vehicle => {
      let marker = this.vehicleMarkers.get(vehicle.id);

      if (!marker) {
        // Create new marker
        marker = this.createVehicleMarker(vehicle);
        this.vehicleMarkers.set(vehicle.id, marker);
      } else {
        // Update existing marker position
        marker.setLatLng([vehicle.lat, vehicle.lng]);

        // Update icon if availability changed
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

  private createVehicleMarker(vehicle: Vehicle): L.Marker {
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
    // Simple SVG car icon without emojis
    const color = available ? '#22c55e' : '#ef4444'; // Green for available, red for in use

    const svgString = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
        <circle cx="16" cy="16" r="14" fill="${color}" stroke="white" stroke-width="2"/>
        <path d="M10 14 L16 10 L22 14 L22 20 L20 22 L12 22 L10 20 Z" fill="white"/>
        <circle cx="13" cy="20" r="1.5" fill="${color}"/>
        <circle cx="19" cy="20" r="1.5" fill="${color}"/>
      </svg>
    `;

    return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svgString);
  }
}
