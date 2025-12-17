import { Component, Input } from '@angular/core';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { FormsModule } from '@angular/forms'; // <-- dodaj ovo

@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [Button, InputComponent, FormsModule], // <-- FormsModule ovde
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
