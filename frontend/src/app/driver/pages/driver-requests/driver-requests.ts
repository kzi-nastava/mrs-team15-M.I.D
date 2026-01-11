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
    // navigate to change-request page with both original and changed driver
    this.router.navigate(['/change-request'], {
      state: { changedDriver: req.changedDriver, originalDriver: req.originalDriver },
    });
  }
}
