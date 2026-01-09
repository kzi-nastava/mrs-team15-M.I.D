import { Component } from '@angular/core';
import { RideOrderingForm } from '../../components/ride-ordering-form/ride-ordering-form';
import { MapComponent } from '../../../shared/components/map/map';

@Component({
  selector: 'app-ride-ordering',
  imports: [RideOrderingForm, MapComponent],
  templateUrl: './ride-ordering.html',
  styleUrl: './ride-ordering.css',
})
export class RideOrdering {

}
