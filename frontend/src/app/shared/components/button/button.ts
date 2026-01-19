import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-button',
  standalone: true,
  templateUrl: './button.html',
  styleUrl: './button.css',
  imports: [RouterLink]
})
export class Button {
  @Input() text: string = '';
  @Input() width: string = '100%';
  @Input() height: string = '100%';
  @Input() fontWeight: string = '100';
  @Input() textTransform: string = 'none';
  @Input() variant: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline' | 'black-outline' = 'primary';
  
  @Input() routerLink?: string;
  @Input() disabled: boolean = false;

  @Output() clicked = new EventEmitter<void>();

  onClick() {
    this.clicked.emit();
  }
}
