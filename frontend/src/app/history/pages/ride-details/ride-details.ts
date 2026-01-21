import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, PLATFORM_ID, Inject, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Ride } from '../../components/ride-history-table/ride-history-table';
import { RideService } from '../../../services/ride.service';

@Component({
  selector: 'app-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.html',
  styleUrl: './ride-details.css'
})
export class RideDetails implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapCanvas', { static: false }) mapCanvas!: ElementRef<HTMLCanvasElement>;
  ride: Ride | null = null;
  rideId: number = 0;
  private map: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private rideService: RideService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // Get ride data from navigation state in constructor
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state) {
      this.ride = navigation.extras.state['ride'];
    }
  }

  ngOnInit(): void {
    // Get ride ID from route params
    this.route.params.subscribe(params => {
      this.rideId = +params['id'];
    });

    // Try to get ride data from history state if not already set
    if (!this.ride) {
      const state = history.state;
      if (state && state['ride']) {
        this.ride = state['ride'];
      }
    }
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.drawMap();
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  async drawMap(): Promise<void> {
    const L = await import('leaflet');

    if (!this.ride || !this.ride.routeData) {
      console.error('No route data available for map');
      return;
    }

    const routeData = this.ride.routeData;

    // Initialize the map centered on the start location
    this.map = L.map('rideMap').setView(
      [routeData.startLocation.latitude, routeData.startLocation.longitude],
      13
    );

    // Set up the OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Fetch the actual route from the estimate endpoint
    this.rideService.estimate({
      startAddress: routeData.startLocation.address,
      destinationAddress: routeData.endLocation.address
    }).subscribe({
      next: (response) => {
        if (response && response.route) {
          // Draw the route using the fetched route data
          const routeCoordinates: [number, number][] = response.route.map(
            (point: any) => [point.lat, point.lng] as [number, number]
          );

          // Draw the route as a polyline
          const routeLine = L.polyline(routeCoordinates, {
            color: '#0d6efd',
            weight: 4,
            opacity: 0.8
          }).addTo(this.map);

          // Create custom icons for start and end points
          const startIcon = L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: #28a745; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">
               <span style="color: white; font-weight: bold; font-size: 18px;">A</span>
             </div>`,
            iconSize: [30, 30],
            iconAnchor: [15, 15]
          });

          const endIcon = L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: #dc3545; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">
               <span style="color: white; font-weight: bold; font-size: 18px;">B</span>
             </div>`,
            iconSize: [30, 30],
            iconAnchor: [15, 15]
          });

          // Add start marker
          L.marker(routeCoordinates[0], { icon: startIcon })
            .addTo(this.map)
            .bindPopup(`<b>Start</b><br>${routeData.startLocation.address}`);

          // Add end marker
          L.marker(routeCoordinates[routeCoordinates.length - 1], { icon: endIcon })
            .addTo(this.map)
            .bindPopup(`<b>End</b><br>${routeData.endLocation.address}`);

          // Fit the map to show the entire route
          this.map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });
        }
      },
      error: (err) => {
        console.error('Error fetching route:', err);
        // Fallback: draw simple line between start and end if estimate fails
        const routeCoordinates: [number, number][] = [
          [routeData.startLocation.latitude, routeData.startLocation.longitude],
          ...routeData.stopLocations.map(stop => [stop.latitude, stop.longitude] as [number, number]),
          [routeData.endLocation.latitude, routeData.endLocation.longitude]
        ];

        const routeLine = L.polyline(routeCoordinates, {
          color: '#0d6efd',
          weight: 4,
          opacity: 0.8
        }).addTo(this.map);

        const startIcon = L.divIcon({
          className: 'custom-marker',
          html: `<div style="background-color: #28a745; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">
               <span style="color: white; font-weight: bold; font-size: 18px;">A</span>
             </div>`,
          iconSize: [30, 30],
          iconAnchor: [15, 15]
        });

        const endIcon = L.divIcon({
          className: 'custom-marker',
          html: `<div style="background-color: #dc3545; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">
               <span style="color: white; font-weight: bold; font-size: 18px;">B</span>
             </div>`,
          iconSize: [30, 30],
          iconAnchor: [15, 15]
        });

        L.marker(routeCoordinates[0], { icon: startIcon })
          .addTo(this.map)
          .bindPopup(`<b>Start</b><br>${routeData.startLocation.address}`);

        L.marker(routeCoordinates[routeCoordinates.length - 1], { icon: endIcon })
          .addTo(this.map)
          .bindPopup(`<b>End</b><br>${routeData.endLocation.address}`);

        this.map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });
      }
    });
  }

  formatDate(date: string): string {
    const d = new Date(date);
    const day = d.getDate().toString().padStart(2, '0');
    const month = (d.getMonth() + 1).toString().padStart(2, '0');
    const year = d.getFullYear();
    const hours = d.getHours().toString().padStart(2, '0');
    const minutes = d.getMinutes().toString().padStart(2, '0');
    return `${day}-${month}-${year} ${hours}:${minutes}`;
  }

  goBack(): void {
    this.router.navigate(['/driver-history']);
  }
}
