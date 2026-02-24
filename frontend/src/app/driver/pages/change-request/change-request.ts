import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ChangeRequestForm } from '../../components/change-request-form/change-request-form';
import { AdminService } from '../../../services/admin.service';

// Page komponenta za prikaz i review pojedinačnog driver change requesta
@Component({
  selector: 'app-change-request',
  standalone: true,
  imports: [CommonModule, ChangeRequestForm],
  templateUrl: './change-request.html',
  styleUrl: './change-request.css',
})
export class ChangeRequest implements OnInit {
  // Izmenjeni podaci drivera
  changedDriver: any = null;
  // Flag da li se koriste mock podaci
  isMock = false;

  // Mock podaci za testiranje izmenjenog drivera
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
  // Mock podaci za testiranje originalnog drivera
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
  // Originalni podaci drivera
  originalDriver: any = null;
  // Meta podaci o requestu (id, datum, razlog)
  requestMeta: any = null;
  // Poruka o rezultatu akcije
  resultMessage = '';

  constructor(private router: Router, private adminService: AdminService) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    this.changedDriver = navigation?.extras?.state?.['changedDriver'] || history.state?.['changedDriver'] || null;
    this.originalDriver = navigation?.extras?.state?.['originalDriver'] || history.state?.['originalDriver'] || null;
    this.requestMeta = navigation?.extras?.state?.['requestMeta'] || history.state?.['requestMeta'] || null;

    // normalize incoming objects so template comparisons work (phone vs phoneNumber, profileImage vs avatarUrl, vehicle fields)
    this.originalDriver = this.normalizeDriver(this.originalDriver) || this.ORIGINAL_DRIVER;
    this.changedDriver = this.normalizeDriver(this.changedDriver) || this.MOCK_DRIVER;
    if (this.changedDriver === this.MOCK_DRIVER) {
      this.isMock = true;
    }
    // `requestMeta` must be provided by the caller (table/navigation).
    // Do not create a mock request id here — the real request id comes from the requests table.
  }

  // Normalizuje driver objekat mapiranjem različitih API formata u jedinstvenu strukturu
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

  // Dohvata vrednost iz objekta pomoću dot-notation puta (npr. 'vehicle.licensePlate')
  private getValue(obj: any, path: string) {
    try {
      return path.split('.').reduce((acc, k) => (acc ? acc[k] : undefined), obj);
    } catch {
      return undefined;
    }
  }

  // Proverava da li se polje razlikuje između originalnog i izmenjenog drivera
  isFieldChanged(path: string): boolean {
    const a = this.getValue(this.originalDriver, path);
    const b = this.getValue(this.changedDriver, path);
    return a !== b;
  }

  // Odobrava change request pozivom admin API-ja
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

  // Odbija change request pozivom admin API-ja
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

  private getCurrentUser(): any {
    try {
      const raw = localStorage.getItem('user');
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

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
