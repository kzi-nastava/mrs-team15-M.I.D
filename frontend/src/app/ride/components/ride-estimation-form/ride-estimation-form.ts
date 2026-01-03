import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';

@Component({
  selector: 'app-ride-estimation-form',
  imports: [Button, InputComponent],
  templateUrl: './ride-estimation-form.html',
  styleUrl: './ride-estimation-form.css',
})
export class RideEstimationForm {

}
