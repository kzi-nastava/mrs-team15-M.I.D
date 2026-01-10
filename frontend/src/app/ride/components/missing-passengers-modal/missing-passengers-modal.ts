import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from '../../../shared/components/button/button';

declare var bootstrap: any;

@Component({
  selector: 'app-missing-passengers-modal',
  standalone: true,
  imports: [CommonModule, Button],
  templateUrl: './missing-passengers-modal.html',
  styleUrl: './missing-passengers-modal.css',
})
export class MissingPassengersModal {
  @Output() confirm = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  confirmMessage: string = '';
  missingPassengers: string[] = [];
  canStart: boolean = false;

  private modalInstance: any;

  openModal(): void {
    const modalElement = document.getElementById('missingPassengersModal');
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

  doConfirm(): void {
    this.confirm.emit();
    this.closeModal();
  }
}
