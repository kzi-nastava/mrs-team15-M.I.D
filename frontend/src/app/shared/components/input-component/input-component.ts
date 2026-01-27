import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-input-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './input-component.html',
  styleUrl: './input-component.css',
})
export class InputComponent {
  @Input() type: string = 'text';
  @Input() placeholder: string = '';
  @Input() width: string = '100%';
  @Input() height: string = '100%';
  @Input() borderColor: string = '#000000';
  @Input() value: string = '';
  @Input() variant: 'primary' | 'secondary' = 'primary';
  @Output() valueChange = new EventEmitter<string>();
  private _suggestions: Array<{ display: string, raw?: any }> | null = null;
  @Input()
  set suggestions(v: Array<{ display: string, raw?: any }> | null) {
    this._suggestions = v;
    // if focused and new suggestions arrived, show the dropdown
    if (this._isFocused && this._suggestions && this._suggestions.length) this.showSuggestions = true;
    console.debug('InputComponent: suggestions set, count=', this._suggestions ? this._suggestions.length : 0);
  }
  get suggestions() { return this._suggestions; }

  @Output() suggestionSelect = new EventEmitter<any>();

  showSuggestions: boolean = false;
  private _isFocused = false;

  onFocus() {
    this._isFocused = true;
    if (this.suggestions && this.suggestions.length) this.showSuggestions = true;
  }

  onBlur() {
    this._isFocused = false;
    // delay hiding to allow click on suggestion
    setTimeout(() => { this.showSuggestions = false; }, 150);
  }

  selectSuggestion(item: any) {
    const display = item.display ?? (item.display_name || '');
    this.value = display;
    this.valueChange.emit(this.value);
    this.suggestionSelect.emit(item);
    this.showSuggestions = false;
  }

  onInput(event: Event) {
    this.valueChange.emit((event.target as HTMLInputElement).value);
  }
}
