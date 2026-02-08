import { ChangeDetectorRef, Component } from '@angular/core';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { Button } from '../../../shared/components/button/button';
import { AdminService } from '../../../services/admin.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface AdminUser {
  id: number;
  role: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string | null;
  blocked: boolean;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [PageHeaderComponent, CommonModule, FormsModule, Button],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class AdminUsers {
  message: string = '';
  showMessage: boolean = false;

  allUsers: AdminUser[] = [];
  filteredUsers: AdminUser[] = [];
  searchQuery: string = '';
  pageSize: number = 10;
  currentPage: number = 1;

  constructor(private cdr: ChangeDetectorRef, private adminService: AdminService) {}

  ngOnInit(): void {
    this.fetchUsers();
  }

  fetchUsers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (users: any[]) => {
        this.allUsers = (users || []).map(u => ({
          id: u.id,
          role: u.role || '',
          email: u.email || '',
          firstName: u.firstName || '',
          lastName: u.lastName || '',
          phoneNumber: u.phoneNumber || null,
          blocked: !!u.blocked,
        }));
        this.applyFilter();
        setTimeout(() => this.cdr.detectChanges(), 0);
      },
      error: (err) => {
        console.error('Failed to load users', err);
        this.showMessageToast('Failed to load users.');
      }
    });
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false;}, 3000);
  }

  // --- Search & pagination ---
  applyFilter(): void {
    const q = (this.searchQuery || '').trim().toLowerCase();
    if (!q) {
      this.filteredUsers = [...this.allUsers];
    } else {
      this.filteredUsers = this.allUsers.filter(u => {
        return (
          (u.role || '').toLowerCase().includes(q) ||
          (u.email || '').toLowerCase().includes(q) ||
          (u.firstName || '').toLowerCase().includes(q) ||
          (u.lastName || '').toLowerCase().includes(q) ||
          (u.phoneNumber || '').toLowerCase().includes(q)
        );
      });
    }
    this.currentPage = 1;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredUsers.length / this.pageSize));
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  get pagedUsers(): AdminUser[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
  }

  changePage(page: number): void {
    if (page < 1) page = 1;
    if (page > this.totalPages) page = this.totalPages;
    this.currentPage = page;
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 1;
  }

  // Called from PageHeader filter (searchMode) and clear
  onSearch(filterValue: string): void {
    this.searchQuery = filterValue || '';
    this.applyFilter();
  }

  onClearFilter(): void {
    this.searchQuery = '';
    this.applyFilter();
  }
}
