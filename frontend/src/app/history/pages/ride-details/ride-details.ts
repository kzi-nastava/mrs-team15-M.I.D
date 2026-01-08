import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, PLATFORM_ID, Inject, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Ride } from '../../components/ride-history-table/ride-history-table';

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
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    // Get ride ID from route params
    this.route.params.subscribe(params => {
      this.rideId = +params['id'];
    });

    // Get ride data from navigation state
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state) {
      this.ride = navigation.extras.state['ride'];
    }

    // If no state was passed, use placeholder data
    if (!this.ride) {
      this.ride = {
        id: this.rideId,
        route: 'Bulevar oslobođenja, Novi Sad → Aerodrom Nikola Tesla, Beograd',
        passengers: 'Marko Marković, Ana Jovanović',
        date: '2025-12-15T14:30:00',
        duration: '25 min',
        timeRange: '14:30 - 14:55',
        cancelled: null,
        cancelledBy: null,
        cost: '1550 RSD',
        panicButton: null,
        panicBy: null,
        rating: 4.5,
        inconsistencies: ['Passenger reported longer route than expected']
      };
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

    // Initialize the map centered on Novi Sad
    this.map = L.map('rideMap').setView([45.2552, 19.8452], 12);

    // Set up the OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Define route coordinates (simulated route through Novi Sad)
    const routeCoordinates: [number, number][] = [
      [45.2552, 19.8452],  // Start: Bulevar oslobođenja
      [45.2580, 19.8500],
      [45.2610, 19.8550],
      [45.2640, 19.8600],
      [45.2670, 19.8650],
      [45.2700, 19.8700],
      [45.2730, 19.8750],
      [45.2760, 19.8800],
      [45.2790, 19.8850],
      [45.2820, 19.8900]   // End: Aerodrom Nikola Tesla (simulated)
    ];

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
      .bindPopup('<b>Start</b><br>Bulevar oslobođenja, Novi Sad');

    // Add end marker
    L.marker(routeCoordinates[routeCoordinates.length - 1], { icon: endIcon })
      .addTo(this.map)
      .bindPopup('<b>End</b><br>Aerodrom Nikola Tesla, Beograd');

    // Fit the map to show the entire route
    this.map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });
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
