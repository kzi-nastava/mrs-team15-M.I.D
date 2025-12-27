import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Ride } from '../../shared/components/ride-history-table/ride-history-table';

@Component({
  selector: 'app-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.html',
  styleUrl: './ride-details.css'
})
export class RideDetails implements OnInit {
  ride: Ride | null = null;
  rideId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router
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
