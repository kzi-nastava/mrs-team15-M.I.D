import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-input-component',
  standalone: true,
  imports: [],
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
}