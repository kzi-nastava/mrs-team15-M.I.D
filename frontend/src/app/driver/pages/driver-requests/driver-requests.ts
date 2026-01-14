import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DriverRequestsTable } from '../../components/driver-requests-table/driver-requests-table';

@Component({
  selector: 'app-driver-requests',
  standalone: true,
  imports: [CommonModule, DriverRequestsTable],
  templateUrl: './driver-requests.html',
  styleUrl: './driver-requests.css',
})
export class DriverRequestsPage {
  constructor(private router: Router) {}

  openRequest(req: any) {
    // navigate to change-request page with both original (prefer stored user) and changed driver
    let original = req.originalDriver;
    if (req?.originalDriver?._full) {
      const u = req.originalDriver._full;
      const vehicleFromUser = {
        licensePlate: u.licensePlate || req.originalDriver.vehicle?.licensePlate,
        model: u.vehicleModel || req.originalDriver.vehicle?.model,
        seats: u.numberOfSeats || req.originalDriver.vehicle?.seats,
        type: u.vehicleType || req.originalDriver.vehicle?.type,
        petFriendly: u.petFriendly ?? req.originalDriver.vehicle?.petFriendly,
        babyFriendly: u.babyFriendly ?? req.originalDriver.vehicle?.babyFriendly,
      };

      original = {
        firstName: u.firstName,
        lastName: u.lastName,
        phone: u.phoneNumber,
        email: u.email,
        address: u.address,
        avatarUrl: u.profileImage || req.originalDriver.avatarUrl,
        role: (u.role || req.originalDriver.role || 'driver').toLowerCase(),
        activeHours: u.hoursWorkedLast24 || req.originalDriver.activeHours || 0,
        vehicle: vehicleFromUser,
      };
    }

    this.router.navigate(['/change-request'], {
      state: { changedDriver: req.changedDriver, originalDriver: original },
    });
  }
}
