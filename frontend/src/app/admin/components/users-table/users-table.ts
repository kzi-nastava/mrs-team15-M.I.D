import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { Button } from '../../../shared/components/button/button';
import { BlockUserModal } from '../block-users-modal/block-user-modal';
import { UnblockUserModal } from '../unblock-users-modal/unblock-user-modal';
import { AdminService } from '../../../services/admin.service';
import { environment } from '../../../../environments/environment';

export interface AdminUser {
  id: number;
  role: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string | null;
  blocked: boolean;
  imageUrl?: string | null;
}

@Component({
  selector: 'app-users-table',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, Button, BlockUserModal, UnblockUserModal],
  templateUrl: './users-table.html',
  styleUrl: './users-table.css'
})
export class UsersTable {
  message: string = '';
  showMessage: boolean = false;

  filteredUsers: AdminUser[] = [];
  searchQuery: string = '';
  pageSize: number = 10;
  currentPage: number = 1;
  totalUsers: number = 0;
  selectedUser: AdminUser | null = null;
  showBlockModal: boolean = false;
  showUnblockModal: boolean = false;
  // sorting
  sortBy: string | null = null;
  sortDir: 'asc' | 'desc' = 'asc';

  constructor(private cdr: ChangeDetectorRef, private adminService: AdminService) {}

  ngOnInit(): void {
    this.fetchUsers();
  }

  fetchUsers(): void {
    const pageIndex = Math.max(0, this.currentPage - 1);
    this.adminService.getAllUsers(this.searchQuery || undefined, this.sortBy || undefined, this.sortDir || undefined, pageIndex, this.pageSize).subscribe({
      next: (pageRes: any) => {
        const users = (pageRes && pageRes.content) ? pageRes.content : [];
        this.filteredUsers = (users || []).map((u: any) => ({
          id: u.id,
          role: u.role || '',
          email: u.email || '',
          firstName: u.firstName || '',
          lastName: u.lastName || '',
          phoneNumber: u.phoneNumber || null,
          blocked: u.blocked === true ,
          imageUrl: u.profileImage ? environment.backendUrl + u.profileImage : null,
        }));
        this.totalUsers = pageRes && pageRes.totalElements ? pageRes.totalElements : this.filteredUsers.length;
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
    setTimeout(() => { this.showMessage = false; }, 3000);
  }

  setSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }
    // request sorted data from backend
    this.fetchUsers();
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalUsers / this.pageSize));
  }

  get pagedUsers(): AdminUser[] {
    return this.filteredUsers;
  }

  changePage(page: number): void {
    if (page < 1) page = 1;
    if (page > this.totalPages) page = this.totalPages;
    this.currentPage = page;
    this.fetchUsers();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 1;
    this.fetchUsers();
  }

  // PageHeader handlers
  onSearch(filterValue: string): void {
    this.searchQuery = filterValue || '';
    this.fetchUsers();
  }

  onClearFilter(): void {
    this.searchQuery = '';
    this.fetchUsers();
  }

  openBlockModal(u: AdminUser): void {
    this.selectedUser = u;
    this.showBlockModal = true;
  }

  openUnblockModal(u: AdminUser): void {
    this.selectedUser = u;
    this.showUnblockModal = true;
  }

  confirmBlock(): void {
    if (!this.selectedUser) return;
    const id = this.selectedUser.id;
    this.adminService.blockUser(id).subscribe({
      next: () => {
        this.selectedUser!.blocked = true;
        this.showMessageToast('User blocked.');
        this.showBlockModal = false;
        this.fetchUsers();
      },
      error: (err) => {
        console.error('Block failed', err);
        this.showMessageToast('Failed to block user.');
        this.showBlockModal = false;
      }
    });
  }

  confirmUnblock(): void {
    if (!this.selectedUser) return;
    const id = this.selectedUser.id;
    this.adminService.unblockUser(id).subscribe({
      next: () => {
        this.selectedUser!.blocked = false;
        this.showMessageToast('User unblocked.');
        this.showUnblockModal = false;
        this.fetchUsers();
      },
      error: (err) => {
        console.error('Unblock failed', err);
        this.showMessageToast('Failed to unblock user.');
        this.showUnblockModal = false;
      }
    });
  }

  onImgError(u: AdminUser): void {
    u.imageUrl = null;
    this.cdr.detectChanges();
  }
}
