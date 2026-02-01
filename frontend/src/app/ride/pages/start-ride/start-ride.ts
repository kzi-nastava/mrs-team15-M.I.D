import { Component, OnInit } from '@angular/core';
import { MapComponent } from '../../../shared/components/map/map';
import { StartRideForm } from '../../components/start-ride-form/start-ride-form';
import { Router } from '@angular/router';
import { DriverService } from '../../../services/driver.service';

@Component({
  selector: 'app-start-ride',
  imports: [MapComponent, StartRideForm],
  templateUrl: './start-ride.html',
  styleUrls: ['./start-ride.css'],
})
export class StartRide implements OnInit {
  ride: any = null;

  constructor(private router: Router, private driverService: DriverService) {}

  ngOnInit(): void {
    // Try to read ride data from navigation extras state first, fall back to history.state
    const nav = this.router.getCurrentNavigation?.();
    this.ride = nav?.extras?.state?.['ride'] ?? (window.history.state && window.history.state['ride'] ? window.history.state['ride'] : null);
    console.log('StartRide ngOnInit - ride:', this.ride);

    // Fetch driver status to trigger location tracking
    const role = localStorage.getItem('role');
    if (role === 'DRIVER') {
      this.driverService.getMyStatus().subscribe();
    }
  }
}
