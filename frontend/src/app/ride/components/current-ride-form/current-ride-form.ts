import { Component, ViewChild } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { ReportInconsistencyModal } from '../report-inconsistency-modal/report-inconsistency-modal';
import { StopRideModal } from '../stop-ride-modal/stop-ride-modal';
@Component({
  selector: 'app-current-ride-form',
  imports: [Button, CommonModule, ReportInconsistencyModal, StopRideModal],
  templateUrl: './current-ride-form.html',
  styleUrl: './current-ride-form.css',
})
export class CurrentRideForm {
  @ViewChild(ReportInconsistencyModal) reportModal!: ReportInconsistencyModal;

  isDriver : boolean = true;
  isPassenger : boolean = false;

  openReportModal() {
    this.reportModal.openModal();
  }

  handleReportSubmitted(message: string) {
    // Handle the report submission here
    console.log('Report submitted:', message);
    // You can add your API call here
  }

  showStopModal : boolean = false;
  openStopModal() : void {
      this.showStopModal = true;
  }

  onStopConfirmed() {
    alert("The ride is stopped");
    this.showStopModal = false;
  }
}
