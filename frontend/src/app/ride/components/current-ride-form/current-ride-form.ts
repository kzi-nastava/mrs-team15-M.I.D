import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-current-ride-form',
  imports: [Button, CommonModule],
  templateUrl: './current-ride-form.html',
  styleUrl: './current-ride-form.css',
})
export class CurrentRideForm {
  isDriver : boolean = true;
  isPassenger : boolean = false;
}
