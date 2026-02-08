import { Component } from '@angular/core';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { AdminHistoryTable } from '../../components/admin-history-table/admin-history-table';
import { Ride } from '../../components/admin-history-table/admin-history-table';

@Component({
  selector: 'app-admin-history',
  imports: [PageHeaderComponent, AdminHistoryTable],
  templateUrl: './admin-history.html',
  styleUrl: './admin-history.css',
})
export class AdminHistory {

  allRides: Ride[] =[];
  filteredRides: Ride[] = [...this.allRides]
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