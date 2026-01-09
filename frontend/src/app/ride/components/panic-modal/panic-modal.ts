import { Component, EventEmitter, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-panic-modal',
  imports: [Button],
  templateUrl: './panic-modal.html',
  styleUrl: './panic-modal.css',
})
export class PanicModal {
  @Output() close =  new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();

  onClose(){
    this.close.emit();
  }

  onConfirm() {
    this.close.emit();
  }
}