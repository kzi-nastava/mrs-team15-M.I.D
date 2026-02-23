import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DriverRequestsTable } from '../../components/driver-requests-table/driver-requests-table';

// Page component that displays list of all driver change requests
// Allows admin to view and navigate to individual requests for review
@Component({
  selector: 'app-driver-requests',
  standalone: true,
  imports: [CommonModule, DriverRequestsTable],
  templateUrl: './driver-requests.html',
  styleUrl: './driver-requests.css',
})
export class DriverRequestsPage {
  constructor(private router: Router) {}

  // Opens individual change request for detailed review
  // Normalizes and passes both original and changed driver data to detail page
  openRequest(req: any) {
    // Navigate to change-request page with both original and changed driver
    let original = req.originalDriver;
    // If detailed user data is available, normalize it into consistent format
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

    // Navigate with all data passed through router state
    this.router.navigate(['/change-request'], {
      state: { changedDriver: req.changedDriver, originalDriver: original, requestMeta: req.requestMeta || { id: req.id, requestedBy: req.requestedBy, submittedAt: req.submittedAt, reason: req.reason } },
    });
  }
}
