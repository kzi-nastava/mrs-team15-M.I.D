import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // <-- dodaj ovo
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';

@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, FormsModule, InputComponent, Button], // <-- dodaj FormsModule
  templateUrl: './vehicle-form.html',
  styleUrls: ['./vehicle-form.css'],
})
export class VehicleForm {
  @Input() vehicle: any;
}
