import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { UpcomingRide } from '../upcoming-rides-table/upcoming-rides-table';
import { InputComponent } from '../../../shared/components/input-component/input-component';
@Component({
  selector: 'app-cancel-ride-modal',
  standalone: true,
  imports: [Button, InputComponent],
  templateUrl: './cancel-ride-modal.html',
  styleUrl: './cancel-ride-modal.css',
})
export class CancelRideModal {
  @Input() ride! : UpcomingRide;
  @Output() close =  new EventEmitter<void>();
  @Output() confirmCancel = new EventEmitter<{id: number, reason: string}>

  reason : string = '';

  onClose(){
    this.close.emit();
  }

onConfirm() {
    if (!this.reason.trim()) {
      alert('Please enter a reason for cancellation.');
      return;
    }
    this.confirmCancel.emit({ id: this.ride.id, reason: this.reason });
    this.close.emit();
  }
}
