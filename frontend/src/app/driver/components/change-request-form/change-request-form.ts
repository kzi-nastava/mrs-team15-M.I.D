import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../../environments/environment';

// Forma koja prikazuje poređenje originalnih i izmenjenih podataka drivera
@Component({
  selector: 'app-change-request-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-request-form.html',
  styleUrl: './change-request-form.css',
})
export class ChangeRequestForm implements OnInit {
  // Originalni podaci drivera
  @Input() originalDriver: any | null = null;
  // Izmenjeni podaci drivera
  @Input() changedDriver: any | null = null;
  // Meta informacije o requestu
  @Input() requestMeta: any | null = null;
  // Event emitter za odobravanje requesta
  @Output() approve = new EventEmitter<any>();
  // Event emitter za odbijanje requesta
  @Output() reject = new EventEmitter<any>();
  backendUrl : string = environment.backendUrl;

  // Flag za mock podatke
  isMock = false;
  // Admin napomene uz odluku
  adminNotes = '';
  // Tip akcije koju je admin preduzeo
  actionTaken: 'approved' | 'rejected' | null = null;
  // Poruka o rezultatu akcije
  resultMessage = '';

  

  

  ngOnInit(): void {
  }

  private getValue(obj: any, path: string) {
    try {
      return path.split('.').reduce((acc, k) => (acc ? acc[k] : undefined), obj);
    } catch {
      return undefined;
    }
  }

  // Proverava da li je polje promenjeno poređenjem originalnog i novog drivera
  isFieldChanged(path: string): boolean {
    const a = this.getValue(this.originalDriver, path);
    const b = this.getValue(this.changedDriver, path);
    return a !== b;
  }

  // Proverava da li je avatar promenjen
  isAvatarChanged(): boolean {
    const a = this.getValue(this.originalDriver, 'avatarUrl');
    const b = this.getValue(this.changedDriver, 'avatarUrl');
    return a && b ? a !== b : Boolean(a) !== Boolean(b);
  }

  onBack(): void {
    history.back();
  }

  // Emituje event za odobravanje requesta
  approveRequest(): void {
    if (this.actionTaken) return;
    this.actionTaken = 'approved';
    this.resultMessage = 'Approving request...';
    setTimeout(() => {
      this.resultMessage = `Request ${this.requestMeta?.id} approved.`;
      this.approve.emit({ requestId: this.requestMeta?.id, notes: this.adminNotes });
    }, 700);
  }

  // Emituje event za odbijanje requesta
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
