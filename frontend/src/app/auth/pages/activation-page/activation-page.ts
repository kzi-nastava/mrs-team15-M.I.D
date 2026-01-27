import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-activation-page',
  templateUrl: './activation-page.html',
  styleUrl: './activation-page.css',
})
export class ActivationPage implements OnInit {

  showMessage = false;
  message = '';
  token: string | null = null;

  constructor(
    private route: ActivatedRoute, private router: Router, private authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token');
    if (!this.token) {
      this.showMessageToast('Invalid activation link.');
      return;
    }
    this.activate();
  }

  activate(): void {
    this.authService.activate(this.token!).subscribe({
      next: (res) => {
        this.showMessageToast(res.message);
        setTimeout(() => { this.router.navigate(['/login']);}, 4000);
      },
      error: (err) => {
        const msg = err.error?.message ?? 'Activation failed. Please try again.';
        this.showMessageToast(msg);
      }
    });
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
  }
}