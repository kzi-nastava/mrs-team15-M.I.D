import { Component, Input } from '@angular/core';
import { InputComponent } from '../input-component/input-component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './vehicle-form.html',
  styleUrl: './vehicle-form.css',
})
export class VehicleForm {
  @Input() vehicle: any = {
    licensePlate: 'NS123AB',
    model: 'Golf 7',
    seats: 4,
    type: 'Standard',
    petFriendly: true,
    babyFriendly: true,
  };

  onUploadPhoto(): void {
    console.log('Upload vehicle photo clicked');
  }

  onSave(): void {
    console.log('Save vehicle', this.vehicle);
  }
}
