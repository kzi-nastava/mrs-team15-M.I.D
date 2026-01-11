import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ChangeRequestForm } from '../../components/change-request-form/change-request-form';

@Component({
  selector: 'app-change-request',
  standalone: true,
  imports: [CommonModule, ChangeRequestForm],
  templateUrl: './change-request.html',
  styleUrl: './change-request.css',
})
export class ChangeRequest implements OnInit {
  changedDriver: any = null;
  isMock = false;

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
  originalDriver: any = null;
  requestMeta: any = null;
  resultMessage = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    this.changedDriver = navigation?.extras?.state?.['changedDriver'] || history.state?.['changedDriver'] || null;
    this.originalDriver = navigation?.extras?.state?.['originalDriver'] || history.state?.['originalDriver'] || null;
    if (!this.changedDriver) {
      this.changedDriver = this.MOCK_DRIVER;
      this.isMock = true;
    }
    if (!this.originalDriver) {
      this.originalDriver = this.ORIGINAL_DRIVER;
    }
    if (!this.requestMeta) {
      this.requestMeta = {
        id: 'REQ-2026-001',
        requestedBy: `${this.changedDriver.firstName} ${this.changedDriver.lastName}`,
        submittedAt: new Date().toISOString(),
        reason: 'Updated vehicle details and contact info',
      };
    }
  }

  private getValue(obj: any, path: string) {
    try {
      return path.split('.').reduce((acc, k) => (acc ? acc[k] : undefined), obj);
    } catch {
      return undefined;
    }
  }

  isFieldChanged(path: string): boolean {
    const a = this.getValue(this.originalDriver, path);
    const b = this.getValue(this.changedDriver, path);
    return a !== b;
  }

  onApprove(event: any): void {
    this.resultMessage = `Approved ${event?.requestId || this.requestMeta?.id}`;
  }

  onReject(event: any): void {
    this.resultMessage = `Rejected ${event?.requestId || this.requestMeta?.id}`;
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
