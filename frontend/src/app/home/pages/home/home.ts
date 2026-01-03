import { Component } from '@angular/core';
import { MapComponent } from '../../../shared/components/map/map';
import { WelcomeForm } from '../../components/welcome-form/welcome-form'

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MapComponent, WelcomeForm],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

}
