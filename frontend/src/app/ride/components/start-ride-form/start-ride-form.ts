import { Component, ViewChild, Input, OnInit } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { DriverService } from '../../../services/driver.service';
import { MissingPassengersModal } from '../missing-passengers-modal/missing-passengers-modal';
import { UpcomingRide } from '../upcoming-rides-table/upcoming-rides-table';

@Component({
  selector: 'app-start-ride-form',
  imports: [Button, CommonModule, MissingPassengersModal],
  templateUrl: './start-ride-form.html',
  styleUrl: './start-ride-form.css',
})

export class StartRideForm implements OnInit {
  @Input() ride?: UpcomingRide | null;
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private driverService: DriverService
  ) {}

  // Modal handling via MissingPassengersModal
  @ViewChild('missingModal') missingModal!: MissingPassengersModal;
  confirmMessage: string = '';
  missingPassengers: string[] = [];
  canStart: boolean = false;
  presentCount: number = 0;

  startRide(): void {
    // compute missing passengers
    this.missingPassengers = this.passengers.filter(p => !p.present).map(p => p.name);
    this.presentCount = this.passengers.filter(p => p.present).length;
    this.canStart = this.presentCount > 0;

    if (this.presentCount === 0) {
      this.confirmMessage = 'No passengers are present. You cannot start the ride.';
      // show modal to inform driver nothing is present
    } else if (this.missingPassengers.length > 0) {
      this.confirmMessage = 'The following passengers are not present:';
      // show modal listing missing passengers and allow confirm
    } else {
      // all passengers present -> attempt to call backend to start ride
      const idParam = this.route.snapshot.queryParams['id'];
      const rideId = this.ride && this.ride.id ? this.ride.id : (idParam ? +idParam : NaN);
      if (Number.isFinite(rideId)) {
        this.driverService.startRide(rideId).subscribe({
          next: () => this.router.navigate(['/current-ride']),
          error: (err) => {
            console.error('Failed to start ride', err);
            // fallback: navigate locally
            this.router.navigate(['/current-ride']);
          }
        });
      } else {
        // no ride id provided — fall back to previous local navigation
        this.router.navigate(['/current-ride']);
      }
      return;
    }

    // open the reusable modal component for the other cases
    if (this.missingModal) {
      this.missingModal.confirmMessage = this.confirmMessage;
      this.missingModal.missingPassengers = this.missingPassengers;
      this.missingModal.canStart = this.canStart;
      this.missingModal.openModal();
    }
  }

  closeConfirmModal(): void {
    // delegate to modal component
    if (this.missingModal) {
      this.missingModal.closeModal();
    }
  }

  confirmStart(): void {
    if (!this.canStart) {
      // do nothing if not allowed to start
      return;
    }
    // Proceed to start ride
    this.closeConfirmModal();
    const idParam = this.route.snapshot.queryParams['id'];
    const rideId = this.ride && this.ride.id ? this.ride.id : (idParam ? +idParam : NaN);
    if (Number.isFinite(rideId)) {
      this.driverService.startRide(rideId).subscribe({
        next: () => this.router.navigate(['/current-ride']),
        error: (err) => {
          console.error('Failed to start ride', err);
          // still navigate as a soft fallback
          this.router.navigate(['/current-ride']);
        }
      });
    } else {
      this.router.navigate(['/current-ride']);
    }
  }
  
  // Example passengers list — replace with real data as needed
  passengers: { name: string; present: boolean }[] = [
    { name: 'Marko Marković', present: false },
    { name: 'Ana Jovanović', present: false },
  ];

  trackByPassenger(index: number, passenger: { name: string; present: boolean }) {
    return passenger.name;
  }

  togglePassenger(passenger: { name: string; present: boolean }) {
    passenger.present = !passenger.present;
    // update present count reactively so template reflects state immediately
    this.presentCount = this.passengers.filter(p => p.present).length;
  }

  ngOnInit(): void {
    // If ride data is provided via navigation state, initialize passengers list
    if (this.ride && this.ride.passengers) {
      // assume `ride.passengers` is a comma-separated string
      this.passengers = this.ride.passengers.split(',').map((n: string) => ({ name: n.trim(), present: false }));
      this.presentCount = this.passengers.filter(p => p.present).length;
    }
  }
}
