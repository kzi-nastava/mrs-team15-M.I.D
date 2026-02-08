import { ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';

export interface User{
  id: number;
  name: string;
  surname: string;
  email: string;
  role: string;
}

type SortColumn = 'name' | 'surname' | 'email'| 'role' ;

@Component({
  selector: 'app-users-overview-table',
  imports: [],
  templateUrl: './users-overview-table.html',
  styleUrl: './users-overview-table.css',
})

export class UsersOverviewTable {
  @Input() users: User[] = [];
  @Output() sortChange = new EventEmitter<{ column: string; direction: string }>();

  currentSortColumn: string = '';
  currentSortDirection: 'asc' | 'desc' = 'asc';

  constructor(private router: Router) {}

  sort(column: string): void {
    if (this.currentSortColumn === column) {
      this.currentSortDirection = this.currentSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.currentSortColumn = column;
      this.currentSortDirection = 'asc';
    }

    this.sortChange.emit({ 
      column: this.currentSortColumn, 
      direction: this.currentSortDirection 
    });
  }

  getSortIcon(column: SortColumn): string {
    if (this.currentSortColumn !== column) {
      return '⇅';
    }
    return this.currentSortDirection === 'asc' ? '↑' : '↓';
  }

  viewUserHistory(user: User): void {
    this.router.navigate(['/admin-history', user.id], { state: { user } });
  }
}
