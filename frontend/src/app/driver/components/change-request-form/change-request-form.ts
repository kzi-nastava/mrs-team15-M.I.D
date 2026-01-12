import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-change-request-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-request-form.html',
  styleUrl: './change-request-form.css',
})
export class ChangeRequestForm implements OnInit {
  @Input() originalDriver: any | null = null;
  @Input() changedDriver: any | null = null;
  @Input() requestMeta: any | null = null;
  @Output() approve = new EventEmitter<any>();
  @Output() reject = new EventEmitter<any>();

  isMock = false;
  adminNotes = '';
  actionTaken: 'approved' | 'rejected' | null = null;
  resultMessage = '';

  private readonly MOCK_DRIVER = {
    firstName: 'Ana',
    lastName: 'Marković',
    phone: '0601234567',
    email: 'ana.markovic@example.com',
    address: 'Bulevar oslobođenja 10, Novi Sad',
    avatarUrl: 'assets/pfp/ana-avatar.jpg',
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
    avatarUrl: 'assets/pfp/default-avatar-icon.jpg',
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

  ngOnInit(): void {
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

  isAvatarChanged(): boolean {
    const a = this.getValue(this.originalDriver, 'avatarUrl');
    const b = this.getValue(this.changedDriver, 'avatarUrl');
    return a && b ? a !== b : Boolean(a) !== Boolean(b);
  }

  onBack(): void {
    history.back();
  }

  approveRequest(): void {
    if (this.actionTaken) return;
    this.actionTaken = 'approved';
    this.resultMessage = 'Approving request...';
    setTimeout(() => {
      this.resultMessage = `Request ${this.requestMeta?.id} approved.`;
      this.approve.emit({ requestId: this.requestMeta?.id, notes: this.adminNotes });
    }, 700);
  }

  rejectRequest(): void {
    if (this.actionTaken) return;
    this.actionTaken = 'rejected';
    this.resultMessage = 'Rejecting request...';
    setTimeout(() => {
      this.resultMessage = `Request ${this.requestMeta?.id} rejected.`;
      this.reject.emit({ requestId: this.requestMeta?.id, notes: this.adminNotes });
    }, 700);
  }
}
