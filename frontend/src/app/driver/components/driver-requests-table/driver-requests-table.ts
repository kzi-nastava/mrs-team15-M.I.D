import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../services/admin.service';
import { ChangeDetectorRef } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-driver-requests-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-requests-table.html',
  styleUrl: './driver-requests-table.css',
})
export class DriverRequestsTable implements OnInit {
  @Output() viewRequest = new EventEmitter<any>();
  requests: any[] = [];
  currentAdminId: number | null = null;
  private DEV_ADMIN_ID = 1;
  selectedStatus: string = 'all';
  sortField: string | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';
  backendUrl : string = environment.backendUrl;
  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    let adminId = this.DEV_ADMIN_ID;
    try {
      const userJson = localStorage.getItem('user');
      if (userJson) {
        const parsed = JSON.parse(userJson);
        if (parsed && parsed.id && parsed.role && parsed.role.toLowerCase() === 'admin') {
          adminId = parsed.id;
        }
      }
    } catch (e) {
      console.error('Failed to parse user from localStorage', e);
    }

    this.currentAdminId = adminId;
    this.fetchRequests(adminId);
  }

  private normalizeUser(u: any, dtoFallback?: any): any {
    const vehicle = {
      licensePlate: u.licensePlate || dtoFallback?.licensePlate || null,
      model: u.vehicleModel || dtoFallback?.vehicleModel || null,
      seats: u.numberOfSeats ?? dtoFallback?.numberOfSeats ?? null,
      type: u.vehicleType || dtoFallback?.vehicleType || null,
      petFriendly: u.petFriendly ?? dtoFallback?.petFriendly ?? false,
      babyFriendly: u.babyFriendly ?? dtoFallback?.babyFriendly ?? false,
    };

    return {
      driverId: u.id || dtoFallback?.driverId || null,
      _full: u,
      _loaded: false,
      firstName: u.firstName || dtoFallback?.firstName || null,
      lastName: u.lastName || dtoFallback?.lastName || null,
      phone: u.phoneNumber || dtoFallback?.phoneNumber || null,
      role: (u.role || 'driver').toLowerCase(),
      email: u.email || dtoFallback?.email || null,
      address: u.address || dtoFallback?.address || null,
      avatarUrl: u.profileImage || dtoFallback?.profileImage || 'uploads/default.png',
      activeHours: u.hoursWorkedLast24 ?? dtoFallback?.activeHours ?? 0,
      vehicle,
    };
  }

  fetchRequests(adminId: number) {
    this.adminService.getDriverRequests().subscribe({
      next: (res: any[]) => {
        console.debug('getDriverRequests response', res);
        
        const list = (res || []).map((dto, idx) => ({
          _backendDto: dto,
          id: dto.id,
          submittedAt: dto.submitDate,
          status: (dto.status || '').toLowerCase(),
          requestedBy: `${dto.firstName} ${dto.lastName}`,
          reason: 'Driver change request',
          
          originalDriver: {
            driverId: dto.driverId ?? null,
            _loaded: false,
            firstName: null,
            lastName: null,
            phone: null,
            role: 'driver',
            email: null,
            address: null,
            avatarUrl: 'uploads/default.png',
            activeHours: 0,
            vehicle: { licensePlate: null, model: null, seats: null, petFriendly: false, babyFriendly: false },
          },
              changedDriver: this.normalizeDto(dto)
        }));
        
        const driverIds = (res || []).map(dto => dto?.driverId ?? null);
        console.log('DriverRequestsTable: driverIds extracted from DTOs', driverIds);
        const userFetchObservables = (res || []).map((dto, idx) => {
          const driverId = dto?.driverId ?? null;
          if (driverId) console.log(`DriverRequestsTable: will fetch user for request idx=${idx} driverId=${driverId}`);
          return driverId ? this.adminService.getUserById(driverId) : of(null);
        });

        if (userFetchObservables.length === 0) {
          this.requests = list;
          try { this.cdr.detectChanges(); } catch (e) { }
          return;
        }

        forkJoin(userFetchObservables).subscribe({
          next: (users: any[]) => {
            console.log('DriverRequestsTable: forkJoin returned users array', users);
            
            users.forEach((u, i) => {
              const dto = res[i];
              console.log('DriverRequestsTable: fetched user for request', i, u);
              if (u) {
                list[i].originalDriver = this.normalizeUser(u, dto);
                list[i].originalDriver._loaded = true;
              } else {
                // fallback: use admin DTO to populate originalDriver so UI shows current-like values
                console.log('DriverRequestsTable: no stored user, falling back to DTO for request', i, dto);
                list[i].originalDriver = this.normalizeDto(dto) || list[i].originalDriver;
                if (list[i].originalDriver) list[i].originalDriver._loaded = true;
              }
            });
            this.requests = list;
            try { this.cdr.detectChanges(); } catch (e) {}
          },
          error: (err) => {
            console.error('Failed to fetch users for driver requests', err);
            
            this.requests = list;
            try { this.cdr.detectChanges(); } catch (e) { }
          }
        });
      },
      error: (err) => {
        console.error('Failed to load driver requests', err);
        
      }
    });
  }

  private normalizeDto(dto: any): any {
    if (!dto) return null;
    return {
      firstName: dto.firstName || null,
      lastName: dto.lastName || null,
      phone: dto.phoneNumber || null,
      role: 'driver',
      email: dto.email || null,
      address: dto.address || null,
      avatarUrl: dto.profileImage || 'uploads/default.png',
      vehicle: {
        licensePlate: dto.licensePlate || null,
        model: dto.vehicleModel || null,
        seats: dto.numberOfSeats ?? null,
        type: dto.vehicleType || null,
        petFriendly: dto.petFriendly ?? false,
        babyFriendly: dto.babyFriendly ?? false,
      }
    };
  }
  
  
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
    const emit = (original: any) => {
      this.viewRequest.emit({
        originalDriver: original,
        changedDriver: req.changedDriver,
        requestMeta: {
          id: req.id,
          requestedBy: req.requestedBy,
          submittedAt: req.submittedAt,
          reason: req.reason
        }
      });
    };

    if (req.originalDriver && req.originalDriver._loaded === false && req.originalDriver.driverId) {
      this.adminService.getUserById(req.originalDriver.driverId).subscribe({
        next: (u: any) => {
          const original = this.normalizeUser(u, req._backendDto);
          original._loaded = true;
          emit(original);
        },
        error: () => emit(req.originalDriver)
      });
      return;
    }

    emit(req.originalDriver);
 }
}
