import { Component, ViewChild } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { ReportInconsistencyModal } from '../report-inconsistency-modal/report-inconsistency-modal';
import { StopRideModal } from '../stop-ride-modal/stop-ride-modal';
import { PanicModal } from '../panic-modal/panic-modal';

@Component({
  selector: 'app-current-ride-form',
  imports: [Button, CommonModule, ReportInconsistencyModal, StopRideModal, PanicModal],
  templateUrl: './current-ride-form.html',
  styleUrl: './current-ride-form.css',
})

export class CurrentRideForm {
  @ViewChild(ReportInconsistencyModal) reportModal!: ReportInconsistencyModal;

  isDriver: boolean = false;
  isPassenger: boolean = true;

  showStopModal: boolean = false;
  showPanicModal: boolean = false;

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
}
