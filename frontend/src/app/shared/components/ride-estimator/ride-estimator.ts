import { Component, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ride-estimator',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './ride-estimator.html',
  styleUrl: './ride-estimator.css'
})
export class RideEstimatorComponent {
  @Output() showRoute = new EventEmitter<{from: string, to: string}>();

  fromAddress: string = '';
  toAddress: string = '';

  onShowRoute(): void {
    this.showRoute.emit({
      from: this.fromAddress,
      to: this.toAddress
    });
  }
}
