import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from '../../../shared/components/button/button';

declare var bootstrap: any;

@Component({
  selector: 'app-add-favorite-modal',
  standalone: true,
  imports: [CommonModule, Button],
  templateUrl: './add-favorite-modal.html',
  styleUrl: './add-favorite-modal.css',
})
export class AddFavoriteModal {
  @Output() confirm = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  itemLabel: string = '';
  pickupAddress: string = '';
  destinationAddress: string = '';
  stopAddresses: string[] = [];

  private modalInstance: any;

  openModal(label: string = '', info?: { pickup?: string | null; destination?: string | null; stop?: string[] | null }): void {
    this.itemLabel = label;
    this.pickupAddress = info?.pickup ?? '';
    this.destinationAddress = info?.destination ?? '';
    this.stopAddresses = info?.stop ?? [];
    const modalElement = document.getElementById('addFavoriteModal');
    if (modalElement) {
      this.modalInstance = new bootstrap.Modal(modalElement);
      this.modalInstance.show();
    }
  }

  closeModal(): void {
    if (this.modalInstance) {
      this.modalInstance.hide();
      this.closed.emit();
      this.clearInfo();
    }
  }

  // clear stored info after close
  private clearInfo(): void {
    this.itemLabel = '';
    this.pickupAddress = '';
    this.destinationAddress = '';
    this.stopAddresses = [];
  }

  doConfirm(): void {
    this.confirm.emit();
    this.closeModal();
  }
}
