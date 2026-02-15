import { ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { Button } from '../../../shared/components/button/button';
import { PanicAlert, NotificationWebSocketService } from '../../../services/notification-websocket.service';
import { PanicAlertService } from '../../../services/panic-alert.service';
import { DatePipe } from '@angular/common';
import { Subscription } from 'rxjs';
import { AdminService } from '../../../services/admin.service';
import { Router } from '@angular/router';

interface PanicAlertResponse {
  content: any[];
  number: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

@Component({
  selector: 'app-panic-alerts',
  imports: [PageHeaderComponent, Button, DatePipe],
  templateUrl: './panic-alerts.html',
  styleUrl: './panic-alerts.css',
})

export class PanicAlerts implements OnInit, OnDestroy {
  allAlerts: PanicAlert[] = [];
  filteredAlerts: PanicAlert[] = [];
  
  currentPage: number = 0;
  pageSize: number = 8;
  totalPages: number = 0;
  totalElements: number = 0;
  isFirstPage: boolean = true;
  isLastPage: boolean = false;
  message = '';
  showMessage = false;

  private alertSubscription?: Subscription;
  
  constructor(
    private panicAlertService: PanicAlertService, 
    private notificationService: NotificationWebSocketService,
    private adminService: AdminService,
    private cdr: ChangeDetectorRef,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadAlerts(this.currentPage);

    this.alertSubscription = this.notificationService.unresolvedAlerts$.subscribe(wsAlerts => {
      this.loadAlerts(this.currentPage);
    });
  }

  ngOnDestroy(): void {
    this.alertSubscription?.unsubscribe();
  }

  private loadAlerts(page: number = 0): void {
    this.panicAlertService.getUnresolvedAlerts(page, this.pageSize).subscribe({
      next: (data: PanicAlertResponse) => {
        this.allAlerts = this.transformAlertData(data.content);
        this.filteredAlerts = [...this.allAlerts];
        this.currentPage = data.number;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isFirstPage = data.first;
        this.isLastPage = data.last;
        if (this.filteredAlerts.length === 0 && !this.isFirstPage) {
          this.goToPreviousPage();
        }
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading alerts:', error);
      }
    });
  }

  private transformAlertData(apiData: any[]): PanicAlert[] {
    console.log(apiData);
    return apiData.map((alert) => ({
      id: alert.id ?? 0,
      rideId: alert.rideId ?? 0,
      panicBy: alert.panicBy ?? 'N/A',
      panicByRole: alert.panicByRole ?? 'N/A',
      createdAt: alert.createdAt ?? new Date().toISOString(),
      resolved: alert.resolved ?? false,
      resolvedAt: alert.resolvedAt,
      resolvedBy: alert.resolvedBy,
      resolvedByEmail: alert.resolvedByEmail,
      driverEmail: alert.driverEmail ?? 'N/A',
      passengerEmail: alert.panicBy ?? 'N/A',
      location: alert.location
    }));
  }

  goToNextPage(): void {
    if (!this.isLastPage) {
      this.currentPage++;
      this.loadAlerts(this.currentPage);
    }
  }

  goToPreviousPage(): void {
    if (!this.isFirstPage) {
      this.currentPage--;
      this.loadAlerts(this.currentPage);
    }
  }

    onAlertRowClick(alert: PanicAlert): void {
    this.adminService.getActiveRides().subscribe({
      next: (rides) => {
        const ride = rides.find((r: { rideId: number; }) => r.rideId === alert.rideId);
        if (ride) {
          this.router.navigate(['/current-ride'], { 
            state: { 
              ride: ride,
              fromAdmin: true,
              isPanic: true
            } 
          });
        } else {
          this.showMessageToast('Ride not found or no longer active');
        }
      },
      error: (error) => {
        console.error('Error fetching ride:', error);
        this.showMessageToast('Error loading ride details');
      }
    });
  }

resolveAlert(alert: PanicAlert, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    this.panicAlertService.resolvePanicAlert(alert.id).subscribe({
      next: (res: string) => {
        this.showMessageToast(res);
        this.filteredAlerts = this.filteredAlerts.filter(a => a.id !== alert.id);
        this.totalElements--;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error resolving alert:', error);
        this.showMessageToast('There was an error resolving the alert. Please try again.');
      }
    });
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false;}, 3000);
  }
}