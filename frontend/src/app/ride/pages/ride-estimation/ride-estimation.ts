import { Component } from '@angular/core';
import { RideEstimationForm } from '../../components/ride-estimation-form/ride-estimation-form';
import { MapComponent } from '../../../shared/components/map/map';

@Component({
  selector: 'app-ride-estimation',
  imports: [RideEstimationForm, MapComponent],
  templateUrl: './ride-estimation.html',
  styleUrl: './ride-estimation.css',
})
export class RideEstimation {

}
