import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { RideService } from '../../../services/ride.service';
import { MapRouteService } from '../../../services/map-route.service';

@Component({
  selector: 'app-stop-ride-modal',
  imports: [Button],
  templateUrl: './stop-ride-modal.html',
  styleUrl: './stop-ride-modal.css',
})
export class StopRideModal {
  @Output() close = new EventEmitter<void>();
  @Output() confirmStop = new EventEmitter<void>();

  constructor(private cdr: ChangeDetectorRef, private rideService : RideService, private mapRouteService : MapRouteService){}

  message = '';
  showMessage = false;

  onClose() {
    this.close.emit();
  }

  onConfirm() {
    this.rideService.stopRide().subscribe({
      next: (response : any) => {
        this.showMessageToast('Ride is stopped successfully.');
        this.cdr.detectChanges();
        this.confirmStop.emit(response);
        setTimeout(() => {this.close.emit();}, 3000);
      },
      error: (err) => {
        if (typeof err.error === 'string') {
          this.showMessageToast(err.error);
          setTimeout(() => {this.close.emit();}, 3000);
        } else {
          this.showMessageToast('Failed to stop ride. Please try again.');
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

