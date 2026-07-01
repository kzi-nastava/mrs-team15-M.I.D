import { ChangeDetectorRef, Component } from '@angular/core';
import { UpcomingRidesTable } from '../../components/upcoming-rides-table/upcoming-rides-table';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { UpcomingRide } from '../../components/upcoming-rides-table/upcoming-rides-table';
import { RideService } from '../../../services/ride.service';
import { DriverService } from '../../../services/driver.service';
import { formatAddress } from '../../../shared/utils/address.utils';

@Component({
  selector: 'app-upcoming-rides',
  standalone: true,
  imports: [UpcomingRidesTable, PageHeaderComponent],
  templateUrl: './upcoming-rides.html',
  styleUrl: './upcoming-rides.css',
})
export class UpcomingRides {
  message: string = '';
  showMessage: boolean = false;
  isDriver: boolean = false;

  constructor(
    private cdr: ChangeDetectorRef,
    private rideService: RideService,
    private driverService: DriverService
  ){}

  allUpcomingRides: UpcomingRide[] = [];
  filteredUpcomingRides: UpcomingRide[] = [...this.allUpcomingRides]

ngOnInit(): void {
  const role = localStorage.getItem('role');
  this.isDriver = role === 'DRIVER';

  if (role !== 'USER' && role !== 'DRIVER') {
    this.showMessageToast('Access denied');
    return;
  }

  const ridesObservable = this.isDriver
    ? this.driverService.getUpcomingRides()
    : this.rideService.getMyUpcomingRides();

  ridesObservable.subscribe({
    next: (rides) => {
      // Transform route addresses to shortened format
      this.allUpcomingRides = rides.map(ride => ({
        ...ride,
        route: this.shortenRouteAddresses(ride.route)
      }));
      this.filteredUpcomingRides = [...this.allUpcomingRides];

      if (rides.length === 0) {
        this.showMessageToast(
          "You don't have any scheduled rides at the moment."
        );
      }

      // Trigger change detection after data is set
      setTimeout(() => this.cdr.detectChanges(), 0);
    },
    error: (err) => {
      console.error('Error fetching upcoming rides:', err);
      this.showMessageToast('Failed to load upcoming rides.');
    }
  });
  }
  onFilter(filterDate: string): void {
    if(filterDate){
      filterDate = formatFilterDate(filterDate)
      this.filteredUpcomingRides = this.allUpcomingRides.filter(UpcomingRide => UpcomingRide.startTime.split(', ')[0] === filterDate);
    }else{
      this.filteredUpcomingRides = [...this.allUpcomingRides];
    }
  }
  onClearFilter(): void {
    this.filteredUpcomingRides = [...this.allUpcomingRides];
  }

  // Utility method to show a temporary message toast to the user, which can be
  // used for success or error notifications. It sets the message and visibility state,
  // triggers change detection to update the UI, and then hides the message after a short delay.
  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false;}, 3000);
  }

  private shortenRouteAddresses(route: string): string {
    // Route format is typically "Start Address → End Address"
    const parts = route.split(' → ');
    if (parts.length === 2) {
      const shortStart = formatAddress(parts[0].trim());
      const shortEnd = formatAddress(parts[1].trim());
      return `${shortStart} → ${shortEnd}`;
    }
    // If format is different, try to shorten anyway
    return formatAddress(route);
  }
}

function formatFilterDate(filterDate: string): string {
  const [year, month, day ] = filterDate.split('-');
  return day + '-' + month + '-' + year;
}
