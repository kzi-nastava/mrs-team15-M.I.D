import { Component, Input } from '@angular/core';

export interface Ride{
  id: number;
  route: string;
  startTime: string;
  endTime: string;
}

type SortColumn = 'route' | 'startTime' | 'endTime' ;
type SortDirection = 'asc' | 'desc' | '';

@Component({
  selector: 'app-user-history-table',
  standalone: true,
  imports: [],
  templateUrl: './user-history-table.html',
  styleUrl: './user-history-table.css',
})
export class UserHistoryTable {
 private _rides : Ride[] = []

  @Input()
  set rides(value: Ride[]) {
    this._rides = value;
    if (this._rides.length > 0) {
      this.applySorting();
    }
  }
  get rides(): Ride[] {
    return this._rides;
  }

  sortColumn: SortColumn | '' = 'startTime';
  sortDirection: SortDirection = 'asc';

  ngOnInit(): void {
    if (this._rides.length > 0) {
      this.applySorting();
    }
  }

  sort(column: SortColumn): void {
    if (this.sortColumn === column) {
      if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc';
      } else if (this.sortDirection === 'desc') {
        this.sortDirection = '';
        this.sortColumn = '';
        return;
      }
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applySorting();
  }

private applySorting(): void {
  if (!this.sortColumn || !this.sortDirection) return;

  this._rides.sort((a, b) => {
    let aValue: any = a[this.sortColumn as SortColumn];
    let bValue: any = b[this.sortColumn as SortColumn];

    if (this.sortColumn === 'startTime' || this.sortColumn == 'endTime') {
      const parse = (value: string) => {
        const [datePart, timePart] = value.split(', ');
        const [day, month, year] = datePart.split('-').map(Number);
        const [hour, minute] = timePart.split(':').map(Number);
        return new Date(year, month - 1, day, hour, minute).getTime();
      };

      aValue = parse(a.startTime);
      bValue = parse(b.startTime);
    }

    else {
      aValue = aValue.toLowerCase();
      bValue = bValue.toLowerCase();
    }

    if (aValue < bValue) {
      return this.sortDirection === 'asc' ? -1 : 1;
    }
    if (aValue > bValue) {
      return this.sortDirection === 'asc' ? 1 : -1;
    }
    return 0;
  });
}

  getSortIcon(column: SortColumn): string {
    if (this.sortColumn !== column) {
      return '⇅';
    }
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }
}