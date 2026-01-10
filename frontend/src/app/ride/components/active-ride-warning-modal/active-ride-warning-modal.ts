import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from '../../../shared/components/button/button';

declare var bootstrap: any;

@Component({
  selector: 'app-active-ride-warning-modal',
  standalone: true,
  imports: [CommonModule, Button],
  templateUrl: './active-ride-warning-modal.html',
  styleUrl: './active-ride-warning-modal.css',
})
export class ActiveRideWarningModal {
  @Output() closed = new EventEmitter<void>();

  private modalInstance: any;

  openModal(): void {
    const modalElement = document.getElementById('activeRideWarningModal');
    if (modalElement) {
      this.modalInstance = new bootstrap.Modal(modalElement);
      this.modalInstance.show();
    }
  }

  closeModal(): void {
    if (this.modalInstance) {
      this.modalInstance.hide();
      this.closed.emit();
    }
  }
}
