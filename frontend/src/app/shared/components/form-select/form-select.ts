import { Component, Input, Output, EventEmitter } from '@angular/core';
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

  @Output() valueChange = new EventEmitter<any>();

  onChange(ev: Event) {
    const val = (ev.target as HTMLSelectElement).value;
    this.value = val;
    this.valueChange.emit(val);
  }
}
