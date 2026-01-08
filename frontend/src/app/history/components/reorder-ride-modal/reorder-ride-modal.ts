import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from '../../../shared/components/button/button';
import { Ride } from '../user-history-table/user-history-table';
import { FormsModule } from '@angular/forms';
import { FromValidator } from '../../../shared/components/form-validator';

@Component({
  selector: 'app-reorder-ride-modal',
  standalone: true,
  imports: [Button, CommonModule, FormsModule],
  templateUrl: './reorder-ride-modal.html',
  styleUrl: './reorder-ride-modal.css',
})

export class ReorderRideModal {

  @Input() ride!: Ride;
  @Output() bookNow = new EventEmitter<void>();
  @Output() schedule = new EventEmitter<{ date: string; time: string }>();
  @Output() close = new EventEmitter<void>();

  showScheduleInputs = false;

  date = '';
  time = '';

  onClose(): void {
    this.close.emit();
  }

  onBookNow(): void {
    this.bookNow.emit();
    this.close.emit();
  }

  onScheduleClick(): void {
    this.showScheduleInputs = true;
  }

  onConfirmSchedule(): void {
    this.schedule.emit({
      date: this.date,
      time: this.time
    });
    this.close.emit();
  }

  onScheduleForLater() {
    this.showScheduleInputs = true;
  }

  validator : FromValidator = new FromValidator();

  hasErrors(){
    return !! (this.validator.dateError(this.date) || this.validator.timeError(this.time))
  }
}