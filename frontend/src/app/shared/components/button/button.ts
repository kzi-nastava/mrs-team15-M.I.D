import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [],
  templateUrl: './button.html',
  styleUrl: './button.css',
})
export class Button {
  @Input() text: string = '';
  @Input() width: string = '100%';
  @Input() height: string = '100%';
  @Input() fontWeight : string = '100';
  @Input() textTransform : string = 'none'
  @Input() variant: 'primary' | 'secondary' | 'danger' | 'ghost' = 'primary';
}