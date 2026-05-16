import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-block-user-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, Button],
  template: `
    <div class="modal-backdrop">
      <div class="modal-card">
        <h5>Block user</h5>
        <p>Are you sure you want to block <strong>{{ user?.firstName }} {{ user?.lastName }}</strong> ({{ user?.email }})?</p>
        <div class="form-group">
          <label for="blockReason">Reason for blocking: <span style="color: red;">*</span></label>
          <textarea 
            id="blockReason"
            class="form-control" 
            [(ngModel)]="reason" 
            placeholder="Enter reason for blocking this user..."
            rows="3"
            maxlength="500"
            required></textarea>
          <small class="text-muted">{{ reason.length }}/500</small>
          <div *ngIf="showError" class="text-danger mt-1">
            <small>Reason is required</small>
          </div>
        </div>
        <div class="d-flex justify-content-end gap-2 mt-3">
          <app-button text="Cancel" variant="secondary" (clicked)="cancel()"></app-button>
          <app-button text="Block" variant="danger" (clicked)="confirm()"></app-button>
        </div>
      </div>
    </div>
  `,
  styles: [
    `:host .modal-backdrop { position: fixed; inset: 0; display:flex; align-items:center; justify-content:center; background: rgba(0,0,0,0.4); z-index:1050; }
     :host .modal-card { background:#fff; padding:1rem 1.25rem; border-radius:8px; width:500px; box-shadow:0 8px 24px rgba(0,0,0,0.2); }
     :host .form-group { margin: 1rem 0; }
     :host .form-group label { display: block; margin-bottom: 0.5rem; font-weight: 500; }
     :host .form-control { width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; font-family: inherit; }
     :host .form-control:focus { outline: none; border-color: #007bff; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25); }`
  ]
})
export class BlockUserModal {
  @Input() user: any;
  @Output() confirmed = new EventEmitter<string>();
  @Output() cancelled = new EventEmitter<void>();

  reason: string = '';
  showError: boolean = false;

  confirm() {
    if (!this.reason.trim()) {
      this.showError = true;
      return;
    }
    this.confirmed.emit(this.reason.trim());
    this.reason = '';
    this.showError = false;
  }

  cancel() {
    this.reason = '';
    this.showError = false;
    this.cancelled.emit();
  }
}
