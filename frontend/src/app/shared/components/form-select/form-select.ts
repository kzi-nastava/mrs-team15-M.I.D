import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-form-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './form-select.html',
  styleUrl: './form-select.css',
})
export class FormSelect {

  @Input() options: any[] = [];
  @Input() value: any;

  // Ako su opcije objekti → koristi ova polja
  @Input() labelKey: string = 'label';
  @Input() valueKey: string = 'value';

  labelOf(option: any): string {
    return typeof option === 'object'
      ? option[this.labelKey]
      : option;
  }

  valueOf(option: any): any {
    return typeof option === 'object'
      ? option[this.valueKey]
      : option;
  }
}
