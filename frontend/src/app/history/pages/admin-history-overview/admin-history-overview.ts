import { ChangeDetectorRef, Component, Input } from '@angular/core';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { User, UsersOverviewTable } from '../../components/users-overview-table/users-overview-table';
import { Button } from '../../../shared/components/button/button';
import { HistoryService, PaginatedUsersResponse, UserResponse } from '../../../services/history.service';

@Component({
  selector: 'app-admin-history-overview',
  imports: [PageHeaderComponent, UsersOverviewTable, Button],
  templateUrl: './admin-history-overview.html',
  styleUrl: './admin-history-overview.css',
})

export class AdminHistoryOverview {
  allUsers: User[] = [];
  filteredUsers: User[] = [];
  
  currentPage: number = 0;
  pageSize: number = 8;
  totalPages: number = 0;
  totalElements: number = 0;
  isFirstPage: boolean = true;
  isLastPage: boolean = false;
  
  currentSortBy: string = 'name'; 
  currentSortDir: string = 'desc';

  constructor(private historyService: HistoryService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadUsers(this.currentPage, this.currentSortBy, this.currentSortDir);
  }

  private loadUsers(page: number = 0, sortBy?: string, sortDir?: string) {
    this.historyService.getUsers(page, this.pageSize, sortBy, sortDir).subscribe({
      next: (data: PaginatedUsersResponse) => {
        this.allUsers = this.transformUserData(data.content);
        this.filteredUsers = [...this.allUsers];
        this.currentPage = data.number;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isFirstPage = data.first;
        this.isLastPage = data.last;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading users:', error);
      }
    });
  }

  private transformUserData(apiData: UserResponse[]): User[] {
    return apiData.map((user) => ({
      id: user.id ?? 0,  
      name: user.name ?? 'N/A',
      surname: user.surname ?? 'N/A',
      role: user.role ?? 'N/A',
      email: user.email ?? 'N/A'
    }));
  }

  onSort(event: { column: string; direction: string }): void {
    this.currentSortBy = event.column;
    this.currentSortDir = event.direction;
    this.currentPage = 0; 
    this.loadUsers(this.currentPage, this.currentSortBy, this.currentSortDir);
  }

  goToNextPage(): void {
    if (!this.isLastPage) {
      this.currentPage++;
      this.loadUsers(this.currentPage, this.currentSortBy, this.currentSortDir);
    }
  }

  goToPreviousPage(): void {
    if (!this.isFirstPage) {
      this.currentPage--;
      this.loadUsers(this.currentPage, this.currentSortBy, this.currentSortDir);
    }
  }
}