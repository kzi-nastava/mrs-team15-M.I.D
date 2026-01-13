import { Component } from '@angular/core';
import { MapComponent } from '../../../shared/components/map/map';
import { FindingDriverForm } from '../../components/finding-driver-form/finding-driver-form';

@Component({
  selector: 'app-finding-driver',
  imports: [MapComponent, FindingDriverForm],
  templateUrl: './finding-driver.html',
  styleUrl: './finding-driver.css',
})
export class FindingDriver {

}
