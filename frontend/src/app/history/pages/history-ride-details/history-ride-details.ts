import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, Inject, OnDestroy, OnInit, PLATFORM_ID, ViewChild } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Ride } from '../../components/user-history-table/user-history-table';
import { Button } from '../../../shared/components/button/button';
import { ReorderRideModal } from '../../components/reorder-ride-modal/reorder-ride-modal';
import { formatAddress } from '../../../shared/utils/address.utils';
import { RideService } from '../../../services/ride.service';

@Component({
  selector: 'app-history-ride-details',
  standalone: true,
  imports: [CommonModule, Button, ReorderRideModal],
  templateUrl: './history-ride-details.html',
  styleUrl: './history-ride-details.css',
})
export class HistoryRideDetails implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapCanvas', { static: false }) mapCanvas!: ElementRef<HTMLCanvasElement>;
  ride: Ride | null = null;
  id!: number;
  private map: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private rideService : RideService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

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
  
      // Use polylinePoints from the route data if available
      const routeCoordinates: [number, number][] = routeData.polylinePoints && routeData.polylinePoints.length > 0
        ? routeData.polylinePoints.map((point: { latitude: number; longitude: number; }) => [point.latitude, point.longitude] as [number, number])
        : [
            [routeData.startLocation.latitude, routeData.startLocation.longitude],
            ...routeData.stopLocations.map((stop: { latitude: number; longitude: number; }) => [stop.latitude, stop.longitude] as [number, number]),
            [routeData.endLocation.latitude, routeData.endLocation.longitude]
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
        .bindPopup(`<b>Start</b><br>${formatAddress(routeData.startLocation.address)}`);
  
      // Add stop location markers
      if (routeData.stopLocations && routeData.stopLocations.length > 0) {
        routeData.stopLocations.forEach((stop: { latitude: number; longitude: number; address: string; }, index: number) => {
          const stopIcon = L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: #ffc107; width: 28px; height: 28px; border-radius: 50%; border: 3px solid white; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">
                   <span style="color: white; font-weight: bold; font-size: 16px;">${index + 1}</span>
                 </div>`,
            iconSize: [28, 28],
            iconAnchor: [14, 14]
          });
  
          L.marker([stop.latitude, stop.longitude], { icon: stopIcon })
            .addTo(this.map)
            .bindPopup(`<b>Stop ${index + 1}</b><br>${formatAddress(stop.address)}`);
        });
      }
  
      // Add end marker
      L.marker(routeCoordinates[routeCoordinates.length - 1], { icon: endIcon })
        .addTo(this.map)
        .bindPopup(`<b>End</b><br>${formatAddress(routeData.endLocation.address)}`);
  
      // Fit the map to show the entire route
      this.map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });
    }
    
  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    // Prefer navigation state (caller passed full ride object)
    const navState = (history && (history as any).state) ? (history as any).state : null;
    if (navState && navState.ride) {
      this.ride = navState.ride as Ride;
      return;
    }
  }
  showReorderModal = false;

  openReorderModal(): void {
    this.showReorderModal = true;
  }

  onBookNow(): void {
  this.rideService.reorderRide(this.id, null).subscribe({
    next: () => {
      this.showMessageToast('Ride reordered successfully');
      this.showReorderModal = false;
    },
    error: (err) => {
      if (typeof err.error === 'string') {
        this.showMessageToast(err.error);
      } else {
        this.showMessageToast('Failed to reorder ride. Please try again.');
      }
    }
  });
}

onScheduleForLater(data: { date: string; time: string }): void {
  const scheduledDateTime = `${data.date}T${data.time}:00`;

  this.rideService.reorderRide(this.id, scheduledDateTime).subscribe({
    next: () => {
      this.showMessageToast('Ride scheduled successfully');
      this.showReorderModal = false;
    },
    error: (err) => {
      if (typeof err.error === 'string') {
        this.showMessageToast(err.error);
      } else {
        this.showMessageToast('Failed to schedule ride. Please try again.');
      }
    }
  });
}


  message = '';
  showMessage = false;

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false;}, 3000);
  }
}