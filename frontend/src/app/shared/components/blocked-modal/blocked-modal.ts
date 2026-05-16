import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-blocked-modal',
  standalone: true,
  imports: [CommonModule, Button],
  templateUrl: './blocked-modal.html',
  styleUrls: ['./blocked-modal.css']
})
export class BlockedModal {
  @Input() reason: string = '';
  @Output() closed = new EventEmitter<void>();

  close() {
    this.closed.emit();
  }
}
