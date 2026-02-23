import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ChangeRequestForm } from '../../components/change-request-form/change-request-form';
import { AdminService } from '../../../services/admin.service';

// Page component for reviewing individual driver change requests
// Displays original vs changed driver data and allows admin to approve or reject
@Component({
  selector: 'app-change-request',
  standalone: true,
  imports: [CommonModule, ChangeRequestForm],
  templateUrl: './change-request.html',
  styleUrl: './change-request.css',
})
export class ChangeRequest implements OnInit {
  // Driver data after requested changes
  changedDriver: any = null;
  // Flag indicating if using mock data
  isMock = false;

  // Mock data for testing when no real driver data is available
  private readonly MOCK_DRIVER = {
    firstName: 'Ana',
    lastName: 'Marković',
    phone: '0601234567',
    email: 'ana.markovic@example.com',
    address: 'Bulevar oslobođenja 10, Novi Sad',
    role: 'driver',
    activeHours: 5,
    vehicle: {
      licensePlate: 'NS123AB',
      model: 'VW Golf',
      seats: 4,
      type: 'Comfort',
      petFriendly: true,
      babyFriendly: false,
    },
  };
  // Mock original driver data before changes (for comparison)
  private readonly ORIGINAL_DRIVER = {
    firstName: 'Ana',
    lastName: 'Marković',
    phone: '0609876543',
    email: 'ana.old@example.com',
    address: 'Jovana Cvijića 5, Novi Sad',
    role: 'driver',
    activeHours: 3,
    vehicle: {
      licensePlate: 'NS999ZZ',
      model: 'Opel Astra',
      seats: 4,
      type: 'Standard',
      petFriendly: false,
      babyFriendly: true,
    },
  };
  // Original driver data before requested changes
  originalDriver: any = null;
  // Metadata about the change request (id, date, etc.)
  requestMeta: any = null;
  // Message displayed to user after approve/reject action
  resultMessage = '';

  constructor(private router: Router, private adminService: AdminService) {}

  ngOnInit(): void {
    // Extract driver data from router navigation state
    const navigation = this.router.getCurrentNavigation();
    this.changedDriver = navigation?.extras?.state?.['changedDriver'] || history.state?.['changedDriver'] || null;
    this.originalDriver = navigation?.extras?.state?.['originalDriver'] || history.state?.['originalDriver'] || null;
    this.requestMeta = navigation?.extras?.state?.['requestMeta'] || history.state?.['requestMeta'] || null;

    // Normalize incoming objects so template comparisons work 
    this.originalDriver = this.normalizeDriver(this.originalDriver) || this.ORIGINAL_DRIVER;
    this.changedDriver = this.normalizeDriver(this.changedDriver) || this.MOCK_DRIVER;
    if (this.changedDriver === this.MOCK_DRIVER) {
      this.isMock = true;
    }
  }

  // Normalizes driver data from different backend formats into consistent structure
  // Handles various field name variations and nested vehicle data
  private normalizeDriver(d: any): any {
    if (!d) return null;
    const vehicleSource = d.vehicle || {};
    const vehicle = {
      licensePlate: d.licensePlate || vehicleSource.licensePlate || null,
      model: d.vehicleModel || vehicleSource.model || null,
      seats: d.numberOfSeats ?? vehicleSource.seats ?? null,
      type: d.vehicleType || vehicleSource.type || null,
      petFriendly: d.petFriendly ?? vehicleSource.petFriendly ?? false,
      babyFriendly: d.babyFriendly ?? vehicleSource.babyFriendly ?? false,
    };

    return {
      firstName: d.firstName || d.changedFirstName || null,
      lastName: d.lastName || d.changedLastName || null,
      phone: d.phone || d.phoneNumber || null,
      email: d.email || null,
      address: d.address || null,
      avatarUrl: d.avatarUrl || d.profileImage || null,
      role: (d.role || 'driver'),
      activeHours: d.activeHours ?? d.hoursWorkedLast24 ?? 0,
      vehicle,
    };
  }

  // Gets nested property value from object using dot-notation path (e.g., 'vehicle.model')
  private getValue(obj: any, path: string) {
    try {
      return path.split('.').reduce((acc, k) => (acc ? acc[k] : undefined), obj);
    } catch {
      return undefined;
    }
  }

  // Checks if a specific field has changed between original and changed driver
  isFieldChanged(path: string): boolean {
    const a = this.getValue(this.originalDriver, path);
    const b = this.getValue(this.changedDriver, path);
    return a !== b;
  }

  // Handles approval of driver change request
  // Sends approval to backend with optional admin notes
  onApprove(event: any): void {
    const rawId = event?.requestId || this.requestMeta?.id;
    const requestId = this.parseRequestId(rawId);
    if (requestId == null) {
      this.resultMessage = 'Invalid request id; cannot approve.';
      return;
    }
    const admin = this.getCurrentUser();
    const adminId = admin?.id || 1;
    const dto = { approved: true, message: event?.notes || '' };
    this.resultMessage = 'Approving...';
    this.adminService.reviewDriverRequest(requestId, dto).subscribe({
      next: () => {
        this.resultMessage = `Approved ${requestId || this.requestMeta?.id}`;
      },
      error: (err) => {
        console.error('Approve failed', err);
        this.resultMessage = 'Approve failed';
      },
    });
  }

  // Handles rejection of driver change request
  // Sends rejection to backend with optional admin notes
  onReject(event: any): void {
    const rawId = event?.requestId || this.requestMeta?.id;
    const requestId = this.parseRequestId(rawId);
    if (requestId == null) {
      this.resultMessage = 'Invalid request id; cannot reject.';
      return;
    }
    const admin = this.getCurrentUser();
    const adminId = admin?.id || 1;
    const dto = { approved: false, message: event?.notes || '' };
    this.resultMessage = 'Rejecting...';
    this.adminService.reviewDriverRequest(requestId, dto).subscribe({
      next: () => {
        this.resultMessage = `Rejected ${requestId || this.requestMeta?.id}`;
      },
      error: (err) => {
        console.error('Reject failed', err);
        this.resultMessage = 'Reject failed';
      },
    });
  }

  // Retrieves current admin user from local storage
  private getCurrentUser(): any {
    try {
      const raw = localStorage.getItem('user');
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  // Parses request ID from various formats (string/number) to valid numeric ID
  private parseRequestId(raw: any): number {
    if (raw == null) return null as any;
    if (typeof raw === 'number') return raw;
    if (typeof raw === 'string') {
      const digits = raw.replace(/\D+/g, '');
      if (!digits) return null as any;
      const n = parseInt(digits, 10);
      return Number.isFinite(n) && n > 0 ? n : null as any;
    }
    return null as any;
  }

  submitRequest(): void {
    this.router.navigate(['/profile'], {
      state: { toastMessage: 'Your change request has been submitted and is pending admin approval.' },
    });
  }

  goBack(): void {
    this.router.navigate(['/profile']);
  }
}
