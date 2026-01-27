import { Component, EventEmitter, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-complete-ride-modal',
  imports: [Button],
  templateUrl: './complete-ride-modal.html',
  styleUrl: './complete-ride-modal.css',
})
export class CompleteRideModal {
  @Output() close = new EventEmitter<void>();
  @Output() confirmComplete = new EventEmitter<void>();

  onClose() {
    this.close.emit();
  }

  onConfirm() {
    this.confirmComplete.emit();
  }
}
