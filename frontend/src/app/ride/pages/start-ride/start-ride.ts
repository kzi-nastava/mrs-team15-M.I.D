import { Component } from '@angular/core';
import { MapComponent } from '../../../shared/components/map/map';
import { StartRideForm } from '../../components/start-ride-form/start-ride-form';

@Component({
  selector: 'app-start-ride',
  imports: [MapComponent, StartRideForm],
  templateUrl: './start-ride.html',
  styleUrl: './start-ride.css',
})
export class StartRide {

}
