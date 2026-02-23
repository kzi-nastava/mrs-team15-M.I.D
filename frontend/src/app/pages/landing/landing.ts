import { Component } from '@angular/core';
import { MapComponent } from '../../shared/components/map/map';
import { RideEstimatorComponent } from '../../shared/components/ride-estimator/ride-estimator';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [MapComponent, RideEstimatorComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing {
  onShowRoute(data: {from: string, to: string}): void {
    console.log('Show route from', data.from, 'to', data.to);
  }
}

