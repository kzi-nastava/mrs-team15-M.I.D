import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './page-header.html',
  styleUrl: './page-header.css'
})
export class PageHeaderComponent {
  @Input() title: string = '';
  @Input() description: string = '';
  @Input() showDateFilter: boolean = false;

  @Output() filter = new EventEmitter<string>();
  @Output() clearFilter = new EventEmitter<void>();

  filterDate: string = '';

  onFilter(): void {
    this.filter.emit(this.filterDate);
  }

  onClearFilter(): void {
    this.filterDate = '';
    this.clearFilter.emit();
  }
}
