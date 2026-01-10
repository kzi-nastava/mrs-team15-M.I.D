import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-start-ride-form',
  imports: [Button, CommonModule],
  templateUrl: './start-ride-form.html',
  styleUrl: './start-ride-form.css',
})

export class StartRideForm {
  constructor(private router: Router) {}

  startRide(): void {
    this.router.navigate(['/current-ride']);
  }
}
