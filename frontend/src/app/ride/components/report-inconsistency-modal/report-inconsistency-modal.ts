import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Button } from '../../../shared/components/button/button';

declare var bootstrap: any;

@Component({
  selector: 'app-report-inconsistency-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, Button],
  templateUrl: './report-inconsistency-modal.html',
  styleUrl: './report-inconsistency-modal.css',
})
export class ReportInconsistencyModal {
  @Output() reportSubmitted = new EventEmitter<string>();

  reportMessage: string = '';
  private modalInstance: any;
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'error';

  openModal() {
    const modalElement = document.getElementById('reportInconsistencyModal');
    if (modalElement) {
      this.modalInstance = new bootstrap.Modal(modalElement);
      this.modalInstance.show();
    }
  }

  closeModal() {
    if (this.modalInstance) {
      this.modalInstance.hide();
      this.reportMessage = '';
    }
  }

  submitReport() {
    if (!this.reportMessage.trim()) {
      this.showToastMessage('You must enter a message before submitting the report.', 'error');
      return;
    }
    this.reportSubmitted.emit(this.reportMessage);
    this.closeModal();
  }

  private showToastMessage(message: string, type: 'success' | 'error' = 'error'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.showToast = true;

    setTimeout(() => {
      this.showToast = false;
    }, 3000);
  }
}
