import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { ReportInconsistencyModal } from '../report-inconsistency-modal/report-inconsistency-modal';
import { StopRideModal } from '../stop-ride-modal/stop-ride-modal';
import { PanicModal } from '../panic-modal/panic-modal';
import { RideService } from '../../../services/ride.service';
import { MapRouteService } from '../../../services/map-route.service';
import { response } from 'express';


export interface CurrentRideDTO {
  estimatedDurationMin: number;
  distanceKm: number;
  route: any;
  startAddress: string;
  endAddress: string;
}


@Component({
  selector: 'app-current-ride-form',
  imports: [Button, CommonModule, ReportInconsistencyModal, StopRideModal, PanicModal],
  templateUrl: './current-ride-form.html',
  styleUrl: './current-ride-form.css',
})

export class CurrentRideForm {
  @ViewChild(ReportInconsistencyModal) reportModal!: ReportInconsistencyModal;

  constructor(private cdr: ChangeDetectorRef, private rideService : RideService, private mapRouteService : MapRouteService){}

  pickupAddress : string = '';
  destinationAddress : string = '';
  message = '';
  showMessage = false;
  estimatedDistanceKm?: number;
  estimatedDurationMin?: number;

  isDriver: boolean = false;
  isPassenger: boolean = true;

  showStopModal: boolean = false;
  showPanicModal: boolean = false;


  ngOnInit(): void {
    const role = localStorage.getItem('role');
    if(role == "DRIVER"){
      this.isDriver = true;
      this.isPassenger = false;
    }else{
      this.isDriver = false;
      this.isPassenger = true;
    }
    this.fetchCurrentRide();
  }
  
 fetchCurrentRide(): void {
  this.rideService.getMyCurrentRide().subscribe({
    next: (response) => {
      this.destinationAddress = response.endAddress;  
      this.pickupAddress = response.startAddress;
      this.estimatedDistanceKm = response.distanceKm;  
      this.estimatedDurationMin = response.estimatedDurationMin;
      this.cdr.detectChanges();
      this.mapRouteService.drawRoute(response.route);
    },
    error: (err) => {
      if (typeof err.error === 'string') {
        this.showMessageToast(err.error);
      } else {
        this.showMessageToast('Getting current ride data failed. Please try again.');
      }
    }
  });
}
  openReportModal() {
    this.reportModal.openModal();
  }

  handleReportSubmitted(message: string) {
    console.log('Report submitted:', message);
  }

  openStopModal(): void {
    this.showStopModal = true;
  }

  onStopConfirmed() {
    alert("The ride is stopped");
    this.showStopModal = false;
  }

  openPanicModal(): void {
    this.showPanicModal = true;
  }

  onPanicConfirmed() {
    alert("Panic is activated");
    this.showPanicModal = false;
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();  
    setTimeout(() => { this.showMessage = false;}, 3000);
  }
}
