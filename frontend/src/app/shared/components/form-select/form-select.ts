import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-form-select',
  standalone: true,
  templateUrl: './form-select.html',
  styleUrl: './form-select.css',
})
export class FormSelect<T = string> {
  /** Options can be array of primitives or objects { value, label } */
  @Input() options: Array<T | { value: T; label: string }> = [];
  @Input() value: T | null = null;
  @Input() placeholder = '';
  @Input() height = '35px';
  @Input() width = '100%';

  @Output() valueChange = new EventEmitter<T | null>();

  onChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    const val = select.value as unknown as T;
    this.value = val;
    this.valueChange.emit(val);
  }

  // Helper to get option label
  labelOf(opt: any): string {
    if (opt == null) return '';
    if (typeof opt === 'object' && 'label' in opt) return String(opt.label);
    return String(opt);
  }

  // Helper to get option value
  valueOf(opt: any): any {
    if (opt == null) return null;
    if (typeof opt === 'object' && 'value' in opt) return opt.value;
    return opt;
  }
}
