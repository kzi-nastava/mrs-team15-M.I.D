import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ride-estimation-form',
  imports: [Button, InputComponent, CommonModule],
  templateUrl: './ride-estimation-form.html',
  styleUrl: './ride-estimation-form.css',
})
export class RideEstimationForm {
  pickupAddress : string = '';
  destinationAddress : string = '';

  validator : FromValidator = new FromValidator();

  hasErrors() : boolean {
    return !!(this.validator.addressError(this.pickupAddress) || this.validator.addressError(this.destinationAddress));
  }

}
