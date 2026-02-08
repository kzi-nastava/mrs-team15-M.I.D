import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-unblock-user-modal',
  standalone: true,
  imports: [CommonModule, Button],
  template: `
    <div class="modal-backdrop">
      <div class="modal-card">
        <h5>Unblock user</h5>
        <p>Are you sure you want to unblock <strong>{{ user?.firstName }} {{ user?.lastName }}</strong> ({{ user?.email }})?</p>
        <div class="d-flex justify-content-end gap-2">
          <app-button text="Cancel" variant="secondary" (clicked)="cancel()"></app-button>
          <app-button text="Unblock" variant="primary" (clicked)="confirm()"></app-button>
        </div>
      </div>
    </div>
  `,
  styles: [
    `:host .modal-backdrop { position: fixed; inset: 0; display:flex; align-items:center; justify-content:center; background: rgba(0,0,0,0.4); z-index:1050; }
     :host .modal-card { background:#fff; padding:1rem 1.25rem; border-radius:8px; width:420px; box-shadow:0 8px 24px rgba(0,0,0,0.2); }`
  ]
})
export class UnblockUserModal {
  @Input() user: any;
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  confirm() {
    this.confirmed.emit();
  }

  cancel() {
    this.cancelled.emit();
  }
}
