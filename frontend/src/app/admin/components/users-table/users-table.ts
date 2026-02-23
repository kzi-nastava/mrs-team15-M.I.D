import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { Button } from '../../../shared/components/button/button';
import { BlockUserModal } from '../block-users-modal/block-user-modal';
import { UnblockUserModal } from '../unblock-users-modal/unblock-user-modal';
import { AdminService } from '../../../services/admin.service';
import { environment } from '../../../../environments/environment';

// Interface defining user data structure for admin panel
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

// Admin component for managing all users (view, search, block/unblock)
// Supports pagination, sorting and search functionality
@Component({
  selector: 'app-users-table',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, Button, BlockUserModal, UnblockUserModal],
  templateUrl: './users-table.html',
  styleUrl: './users-table.css'
})
export class UsersTable {
  // Toast message text
  message: string = '';
  // Toast message visibility
  showMessage: boolean = false;

  // List of users to display
  filteredUsers: AdminUser[] = [];
  // Current search query
  searchQuery: string = '';
  // Number of users per page
  pageSize: number = 10;
  // Current page number
  currentPage: number = 1;
  // Total number of users in database
  totalUsers: number = 0;
  // User selected for block/unblock action
  selectedUser: AdminUser | null = null;
  // Block modal visibility
  showBlockModal: boolean = false;
  // Unblock modal visibility
  showUnblockModal: boolean = false;
  // Field to sort by
  sortBy: string | null = null;
  // Sort direction
  sortDir: 'asc' | 'desc' = 'asc';

  constructor(private cdr: ChangeDetectorRef, private adminService: AdminService) {}

  ngOnInit(): void {
    this.fetchUsers();
  }

  // Fetches users from backend with current filters, sort and pagination
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

  // Displays toast message for 3 seconds
  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showMessage = false; }, 3000);
  }

  // Sets or toggles sort column and direction
  setSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }
    // Request sorted data from backend
    this.fetchUsers();
  }

  // Calculates total number of pages
  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalUsers / this.pageSize));
  }

  // Returns current page of users
  get pagedUsers(): AdminUser[] {
    return this.filteredUsers;
  }

  // Changes to specified page
  changePage(page: number): void {
    if (page < 1) page = 1;
    if (page > this.totalPages) page = this.totalPages;
    this.currentPage = page;
    this.fetchUsers();
  }

  // Updates page size and resets to first page
  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 1;
    this.fetchUsers();
  }

  // Handles search input from page header
  onSearch(filterValue: string): void{
    this.searchQuery = filterValue || '';
    this.fetchUsers();
  }

  // Clears search filter
  onClearFilter(): void {
    this.searchQuery = '';
    this.fetchUsers();
  }

  // Opens block user modal
  openBlockModal(u: AdminUser): void {
    this.selectedUser = u;
    this.showBlockModal = true;
  }

  // Opens unblock user modal
  openUnblockModal(u: AdminUser): void {
    this.selectedUser = u;
    this.showUnblockModal = true;
  }

  confirmBlock(reason: string): void {
    if (!this.selectedUser) return;
    const id = this.selectedUser.id;
    this.adminService.blockUser(id, reason).subscribe({
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

  // Confirms and executes user unblock action
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

  // Handles profile image load error by hiding image
  onImgError(u: AdminUser): void {
    u.imageUrl = null;
    this.cdr.detectChanges();
  }
}
