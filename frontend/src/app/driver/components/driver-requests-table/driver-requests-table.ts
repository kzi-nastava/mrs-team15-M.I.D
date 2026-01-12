import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-driver-requests-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-requests-table.html',
  styleUrl: './driver-requests-table.css',
})
export class DriverRequestsTable {
  @Output() viewRequest = new EventEmitter<any>();

  requests = [
    {
      id: 'REQ-2026-101',
      submittedAt: new Date(Date.now() - 2 * 86400000).toISOString(),
      status: 'pending',
      requestedBy: 'Ana Marković',
      reason: 'Change contact and vehicle info',
      originalDriver: {
        firstName: 'Ana',
        lastName: 'Marković',
        phone: '0609876543',
        role: 'driver',
        email: 'ana.old@example.com',
        address: 'Jovana Cvijića 5, Novi Sad',
        avatarUrl: 'assets/pfp/default-avatar-icon.jpg',
        activeHours: 3,
        vehicle: { licensePlate: 'NS999ZZ', model: 'Opel Astra', seats: 4, petFriendly: false, babyFriendly: true },
      },
      changedDriver: {
        firstName: 'Ana',
        lastName: 'Marković',
        phone: '0601234567',
        role: 'driver',
        email: 'ana.markovic@example.com',
        address: 'Bulevar oslobođenja 10, Novi Sad',
        avatarUrl: 'assets/pfp/default-avatar-icon.jpg',
        
        vehicle: { licensePlate: 'NS123AB', model: 'VW Golf', seats: 4, petFriendly: true, babyFriendly: false },
      },
    },
    {
      id: 'REQ-2026-102',
      submittedAt: new Date(Date.now() - 86400000).toISOString(),
      status: 'pending',
      requestedBy: 'Marko Petrović',
      reason: 'Enable pet friendly option',
      originalDriver: {
        firstName: 'Marko',
        lastName: 'Petrović',
        phone: '0615551234',
        role: 'driver',
        email: 'marko.petrovic@example.com',
        address: 'Bulevar kralja Petra 3, Novi Sad',
        avatarUrl: 'assets/pfp/default-avatar-icon.jpg',
        activeHours: 3,
        vehicle: { licensePlate: 'NS321BC', model: 'Fiat Tipo', seats: 4, petFriendly: false, babyFriendly: true },
      },
      changedDriver: {
        firstName: 'Marko',
        lastName: 'Petrović',
        phone: '0615559999',
        role: 'driver',
        email: 'marko.new@example.com',
        address: 'Bulevar kralja Petra 3, Novi Sad',
        avatarUrl: 'assets/pfp/default-avatar-icon.jpg',
        vehicle: { licensePlate: 'NS321BC', model: 'Fiat Tipo', seats: 4, petFriendly: true, babyFriendly: true },
      },
    },
    {
      id: 'REQ-2026-050',
      submittedAt: new Date(Date.now() - 7 * 86400000).toISOString(),
      status: 'approved',
      requestedBy: 'Jelena Ilić',
      reason: 'Update email and legal name',
      originalDriver: {
        firstName: 'Jelena',
        lastName: 'Ilić',
        phone: '062111222',
        role: 'driver',
        email: 'jelena.old@example.com',
        address: 'Svetozara Miletića 12, Novi Sad',
        avatarUrl:'assets/pfp/default-avatar-icon.jpg',
        activeHours: 3,
        vehicle: { licensePlate: 'NS555AA', model: 'Toyota Corolla', seats: 4, petFriendly: false, babyFriendly: false },
      },
      changedDriver: {
        firstName: 'Jelena',
        lastName: 'Ilić',
        phone: '062111222',
        role: 'driver',
        email: 'jelena.ilic@example.com',
        address: 'Svetozara Miletića 12, Novi Sad',
        avatarUrl: 'assets/pfp/default-avatar-icon.jpg',
        vehicle: { licensePlate: 'NS555AA', model: 'Toyota Corolla', seats: 4, petFriendly: false, babyFriendly: false },
      },
    },
  ];
  
  selectedStatus: string = 'all';
  sortField: string | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  
  get filteredRequests() {
    let list = this.requests;
    if (this.selectedStatus && this.selectedStatus !== 'all') {
      list = list.filter(r => r.status === this.selectedStatus);
    }
    if (this.sortField) {
      list = list.slice().sort((a: any, b: any) => {
        const dir = this.sortDirection === 'asc' ? 1 : -1;
        if (this.sortField === 'submittedAt') {
          return (new Date(a.submittedAt).getTime() - new Date(b.submittedAt).getTime()) * dir;
        }
        const field = this.sortField as string;
        const av = (a as any)[field] ?? '';
        const bv = (b as any)[field] ?? '';
        return String(av).localeCompare(String(bv)) * dir;
      });
    }
    return list;
  }
  
  onFilterChange(status: string) {
    this.selectedStatus = status;
  }

  sortBy(field: string) {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
  }

  onView(req: any) {
    this.viewRequest.emit(req);
  }
}
