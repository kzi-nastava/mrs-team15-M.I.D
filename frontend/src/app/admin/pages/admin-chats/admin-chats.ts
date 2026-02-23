import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { ChatService } from '../../../services/chat.service';
import { Chat } from '../../../model/chat.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { filter, Subscription } from 'rxjs';

@Component({
  selector: 'app-admin-chats',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent],
  templateUrl: './admin-chats.html',
  styleUrls: ['./admin-chats.css']
})
export class AdminChats implements OnInit, OnDestroy {
  chats: Chat[] = [];
  searchTerm: string = '';
  loading = true;
  error: string | null = null;
  private routerSubscription: Subscription | null = null;

  constructor(
    private chatService: ChatService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadChats();

    // Reload chats whenever we navigate to this page
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      if (event.url === '/admin-chats' || event.url.startsWith('/admin-chats')) {
        this.loadChats();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  // Method to load all chats from the backend using the ChatService
  loadChats(): void {
    this.loading = true;
    this.error = null;

    this.chatService.getAllChats().subscribe({
      next: (chats) => {
        this.chats = chats;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading chats:', err);
        this.error = 'Failed to load chats';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Method to navigate to the chat detail page for a specific chat ID
  openChat(chatId: number): void {
    this.router.navigate(['/admin-chat', chatId]);
  }

  // Getter to filter chats based on the search term entered by the admin user
  get filteredChats(): Chat[] {
    if (!this.searchTerm.trim()) {
      return this.chats;
    }

    const term = this.searchTerm.toLowerCase();
    return this.chats.filter(chat =>
      chat.user.toLowerCase().includes(term) ||
      chat.id.toString().includes(term)
    );
  }
}
