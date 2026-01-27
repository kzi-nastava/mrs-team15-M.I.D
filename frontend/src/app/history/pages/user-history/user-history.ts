import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { UserHistoryTable } from '../../components/user-history-table/user-history-table';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { CommonModule } from '@angular/common';
import { Ride } from '../../components/user-history-table/user-history-table';
import { RideHistoryService, RideHistoryResponse } from '../../../services/ride-history.service';
import { formatAddress } from '../../../shared/utils/address.utils';

// Raw DTO from backend may include slight shape differences (string cost, driverName, etc.)
type RawRideHistoryDTO = any;

@Component({
  selector: 'app-user-history',
  standalone: true,
  imports: [UserHistoryTable, PageHeaderComponent, CommonModule],
  templateUrl: './user-history.html',
  styleUrl: './user-history.css',
})
export class UserHistory  {
  allRides: Ride[] = [];
  filteredRides: Ride[] = [];

  constructor(private rideHistoryService: RideHistoryService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  private loadHistory(dateFrom?: string, dateTo?: string, sortBy?: string, sortDirection?: string) {
    this.rideHistoryService.getPassengerRideHistory(dateFrom, dateTo, sortBy, sortDirection).subscribe({
      next: (res: RawRideHistoryDTO[]) => {
        console.log('Fetched ride history:', res);
        try {
          const mapped = (res || []).map((r, idx) => this.mapToRide(r, idx + 1));
          // assign immediately so filteredRides contains all entries as soon as they're available
          this.allRides = mapped;
          this.filteredRides = [...this.allRides];
          // ensure parent view updates (debug panel relies on this)
          Promise.resolve().then(() => { try { this.cdr.detectChanges(); } catch(e){} });
        } catch (e) {
          console.warn('Mapping ride history failed', e);
          this.allRides = [];
          this.filteredRides = [];
        }
      },
      error: (err) => {
        console.warn('Failed to load ride history', err);
        this.allRides = [];
        this.filteredRides = [];
      }
    });
  }

  private mapToRide(r: RawRideHistoryDTO, idx: number): Ride {
    const idVal = (r as any).id ?? idx;

    // route label fallback: prefer route.start/end, then startAddress/endAddress, then id label
    let routeLabel = '';
    if (r.route && r.route.startLocation && r.route.endLocation) {
      const startAddr = formatAddress(r.route.startLocation.address);
      const endAddr = formatAddress(r.route.endLocation.address);
      routeLabel = `${startAddr} → ${endAddr}`;
    } else if (r.startAddress && r.endAddress) {
      const startAddr = formatAddress(r.startAddress);
      const endAddr = formatAddress(r.endAddress);
      routeLabel = `${startAddr} → ${endAddr}`;
    } else {
      routeLabel = `Ride #${idVal}`;
    }

    // start/end time: try parse r.date, fallback to current time
    let startTimeStr = '';
    let endTimeStr = '';
    try {
      if (r.date) {
        startTimeStr = this.formatDateTime(r.date);
      } else {
        startTimeStr = this.formatDateTime(new Date().toISOString());
      }
      if (r.date && typeof r.durationMinutes === 'number') {
        const startDt = new Date(r.date);
        const endDt = new Date(startDt.getTime() + (r.durationMinutes || 0) * 60000);
        endTimeStr = this.formatDateTime(endDt.toISOString());
      } else {
        endTimeStr = startTimeStr;
      }
    } catch (e) {
      startTimeStr = '';
      endTimeStr = '';
    }

    const passengers = Array.isArray(r.passengers) ? r.passengers.join(', ') : (r.passengers as any) || '';

    const driverName = (r as any).driverName ?? (r as any).driver ?? '';
    let costStr = '0 RSD';
    try {
      if ((r as any).cost !== undefined && (r as any).cost !== null) {
        costStr = typeof (r as any).cost === 'number' ? `${(r as any).cost} RSD` : String((r as any).cost);
      } else if (r.cost) {
        costStr = typeof r.cost === 'number' ? `${r.cost} RSD` : String(r.cost);
      }
    } catch (e) {}

    const stopAddrs = r.route?.stopLocations ? (r.route.stopLocations.map((s: any) => s.address) || []) : (r.stopAddresses ?? []);

    return {
      id: idVal,
      route: routeLabel,
      startTime: startTimeStr,
      endTime: endTimeStr,
      passengers: passengers,
      driver: driverName,
      cancelled: r.cancelled ? 'Yes' : null,
      cancelledBy: r.cancelledBy ?? null,
      cost: costStr,
      panicButton: r.panic ? 'Emergency' : null,
      panicBy: r.panicBy ?? null,
      rating: r.rating ? (r.rating.driverRating ?? r.rating) : null,
      inconsistencies: r.inconsistencies ?? null,
      favorite: !!(r as any).favoriteRoute,
      routeId: r.route?.id ?? (r as any).routeId ?? null,
      pickupAddress: r.route?.startLocation?.address ?? r.startAddress ?? null,
      destinationAddress: r.route?.endLocation?.address ?? r.endAddress ?? null,
      stopAddresses: stopAddrs
    } as Ride;
  }

  private formatDateTime(dateIso: string): string {
    try {
      const d = new Date(dateIso);
      if (isNaN(d.getTime())) return '';
      const pad = (n: number) => n < 10 ? '0' + n : String(n);
      const day = pad(d.getDate());
      const month = pad(d.getMonth() + 1);
      const year = d.getFullYear();
      const hours = pad(d.getHours());
      const minutes = pad(d.getMinutes());
      return `${day}-${month}-${year}, ${hours}:${minutes}`;
    } catch (e) {
      return '';
    }
  }

  onFilter(filterDate: string): void {
    if(filterDate){
      filterDate = formatFilterDate(filterDate)
      this.filteredRides = this.allRides.filter(ride => ride.startTime.split(', ')[0] === filterDate);
    }else{
      this.filteredRides = [...this.allRides];
    }
  }
  onClearFilter(): void {
    this.filteredRides = [...this.allRides];
  }
}

function formatFilterDate(filterDate: string): string {
  const [year, month, day ] = filterDate.split('-');
  return day + '-' + month + '-' + year;
}
