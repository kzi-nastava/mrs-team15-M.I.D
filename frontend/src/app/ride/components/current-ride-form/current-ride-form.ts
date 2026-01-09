import { Component, ViewChild } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { ReportInconsistencyModal } from '../report-inconsistency-modal/report-inconsistency-modal';
import { PanicModal } from '../panic-modal/panic-modal';

@Component({
  selector: 'app-current-ride-form',
  imports: [Button, CommonModule, ReportInconsistencyModal, PanicModal],
  templateUrl: './current-ride-form.html',
  styleUrl: './current-ride-form.css',
})
export class CurrentRideForm {
  @ViewChild(ReportInconsistencyModal) reportModal!: ReportInconsistencyModal;

  isDriver : boolean = false;
  isPassenger : boolean = true;

  openReportModal() {
    this.reportModal.openModal();
  }

  handleReportSubmitted(message: string) {
    // Handle the report submission here
    console.log('Report submitted:', message);
    // You can add your API call here
  }

  showPanicModal : boolean = false;
  openPanicModal() : void {
      this.showPanicModal = true;
  }
  
  onPanicConfirmed() {
    alert("Panic is activated");
    this.showPanicModal = false;
  }
}
