import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DriverStatusStore {
  private statusSubject = new BehaviorSubject<string | null>(null);
  status$ = this.statusSubject.asObservable();

  setStatus(status: string) {
    this.statusSubject.next(status);
  }

   resetStatus() {
    this.statusSubject.next(null);  
  }

  get currentStatus(): string | null {
    return this.statusSubject.value;
  }
}