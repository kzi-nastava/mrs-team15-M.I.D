import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsersTable } from '../../components/users-table/users-table';

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
  imports: [CommonModule, UsersTable],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class AdminUsers {}
