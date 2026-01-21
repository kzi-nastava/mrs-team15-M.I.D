import { ChangeDetectorRef, Component } from '@angular/core';
import { UpcomingRidesTable } from '../../components/upcoming-rides-table/upcoming-rides-table';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { UpcomingRide } from '../../components/upcoming-rides-table/upcoming-rides-table';
import { RideService } from '../../../services/ride.service';
import { DriverService } from '../../../services/driver.service';

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
      this.allUpcomingRides = rides;
      this.filteredUpcomingRides = [...rides];

      if (rides.length === 0) {
        this.showMessageToast(
          "You don't have any scheduled rides at the moment."
        );
      }

      this.cdr.detectChanges();
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

  
  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();  
    setTimeout(() => { this.showMessage = false;}, 3000);
  }
}

function formatFilterDate(filterDate: string): string {
  const [year, month, day ] = filterDate.split('-');
  return day + '-' + month + '-' + year;
}