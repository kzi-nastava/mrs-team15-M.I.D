import { Component } from '@angular/core';
import { MapComponent } from '../../../shared/components/map/map';
import { CurrentRideForm } from '../../components/current-ride-form/current-ride-form';

@Component({
  selector: 'app-current-ride',
  imports: [MapComponent, CurrentRideForm],
  templateUrl: './current-ride.html',
  styleUrl: './current-ride.css',
})
export class CurrentRide {

}
