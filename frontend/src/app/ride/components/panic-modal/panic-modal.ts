import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { RideService } from '../../../services/ride.service';
import { MapRouteService } from '../../../services/map-route.service';

@Component({
  selector: 'app-panic-modal',
  imports: [Button],
  templateUrl: './panic-modal.html',
  styleUrl: './panic-modal.css',
})
export class PanicModal {
  @Output() close =  new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();

  constructor(private cdr: ChangeDetectorRef, private rideService : RideService, private mapRouteService : MapRouteService){}

  message = '';
  showMessage = false;

  onClose(){
    this.close.emit();
  }

onConfirm() {
  this.rideService.triggerPanicAlert().subscribe({
    next: () => {
      this.mapRouteService.alertRoute();
      this.showMessageToast('Panic mode activated. Your help is on the way!');
      this.cdr.detectChanges();
      setTimeout(() => {this.close.emit();}, 3000);
    },
    error: (err) => {
      if (typeof err.error === 'string') {
        this.showMessageToast(err.error);
         setTimeout(() => {this.close.emit();}, 3000);
      } else {
        this.showMessageToast('Failed to activate panic alarm. Please try again.');
        setTimeout(() => {this.close.emit();}, 3000);
      }
    }
  });
}

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();  
    setTimeout(() => { this.showMessage = false;}, 3000);
  }
}