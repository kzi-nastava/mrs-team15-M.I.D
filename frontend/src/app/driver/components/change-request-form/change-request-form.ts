import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../../environments/environment';

// Form component that displays comparison between original and changed driver data
// Allows admin to approve or reject the change request
@Component({
  selector: 'app-change-request-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-request-form.html',
  styleUrl: './change-request-form.css',
})
export class ChangeRequestForm implements OnInit {
  // Driver data before requested changes
  @Input() originalDriver: any | null = null;
  // Driver data after requested changes
  @Input() changedDriver: any | null = null;
  // Metadata about the request
  @Input() requestMeta: any | null = null;
  // Event emitted when admin approves request
  @Output() approve = new EventEmitter<any>();
  // Event emitted when admin rejects request
  @Output() reject = new EventEmitter<any>();
  // Backend URL from environment config
  backendUrl : string = environment.backendUrl;

  // Flag indicating if using mock data
  isMock = false;
  // Optional notes from admin
  adminNotes = '';
  // Tracks which action was taken (prevents duplicate submissions)
  actionTaken: 'approved' | 'rejected' | null = null;
  // Result message displayed to user
  resultMessage = '';

  

  

  ngOnInit(): void {
  }

  // Gets nested property value using dot-notation path (e.g., 'vehicle.model')
  private getValue(obj: any, path: string) {
    try {
      return path.split('.').reduce((acc, k) => (acc ? acc[k] : undefined), obj);
    } catch {
      return undefined;
    }
  }

  // Checks if a field has changed between original and changed driver
  isFieldChanged(path: string): boolean {
    const a = this.getValue(this.originalDriver, path);
    const b = this.getValue(this.changedDriver, path);
    return a !== b;
  }

  // Checks if driver's avatar/profile image has changed
  isAvatarChanged(): boolean {
    const a = this.getValue(this.originalDriver, 'avatarUrl');
    const b = this.getValue(this.changedDriver, 'avatarUrl');
    return a && b ? a !== b : Boolean(a) !== Boolean(b);
  }

  // Navigates back to previous page
  onBack(): void {
    history.back();
  }

  // Handles approve button click, emits approve event to parent component
  // Prevents duplicate submissions if action already taken
  approveRequest(): void {
    if (this.actionTaken) return;
    this.actionTaken = 'approved';
    this.resultMessage = 'Approving request...';
    setTimeout(() => {
      this.resultMessage = `Request ${this.requestMeta?.id} approved.`;
      this.approve.emit({ requestId: this.requestMeta?.id, notes: this.adminNotes });
    }, 700);
  }

  // Handles reject button click, emits reject event to parent component
  // Prevents duplicate submissions if action already taken
  rejectRequest(): void {
    if (this.actionTaken) return;
    this.actionTaken = 'rejected';
    this.resultMessage = 'Rejecting request...';
    setTimeout(() => {
      this.resultMessage = `Request ${this.requestMeta?.id} rejected.`;
      this.reject.emit({ requestId: this.requestMeta?.id, notes: this.adminNotes });
    }, 700);
  }
}
