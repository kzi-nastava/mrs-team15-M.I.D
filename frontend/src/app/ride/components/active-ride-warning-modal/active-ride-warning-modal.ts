import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

declare var bootstrap: any;

@Component({
  selector: 'app-active-ride-warning-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './active-ride-warning-modal.html',
  styleUrl: './active-ride-warning-modal.css',
})
export class ActiveRideWarningModal {
  private modalInstance: any;

  openModal(): void {
    const modalElement = document.getElementById('activeRideWarningModal');
    if (modalElement) {
      this.modalInstance = new bootstrap.Modal(modalElement);
      this.modalInstance.show();
    }
  }
}
