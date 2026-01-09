import { Component, EventEmitter, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-stop-ride-modal',
  imports: [Button],
  templateUrl: './stop-ride-modal.html',
  styleUrl: './stop-ride-modal.css',
})
export class StopRideModal {
   @Output() close = new EventEmitter<void>();
  @Output() confirmStop = new EventEmitter<void>();

  onClose() {
    this.close.emit();
  }

  onConfirm() {
    this.confirmStop.emit(); 
    this.close.emit();       
  }
}

