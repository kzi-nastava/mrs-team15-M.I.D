
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { MapRouteService } from '../../../services/map-route.service';

@Component({
  selector: 'app-welcome-form',
  standalone: true,
  imports: [Button, CommonModule],
  templateUrl: './welcome-form.html',
  styleUrls: ['./welcome-form.css'],
})
export class WelcomeForm {
  protected isLoggedIn: boolean = false;

  constructor(private mapRouteService: MapRouteService) {
    this.isLoggedIn = !!(
      localStorage.getItem('user') ||
      localStorage.getItem('token') ||
      sessionStorage.getItem('user') ||
      sessionStorage.getItem('token')
    );
  }

  ngOnInit() {
    this.mapRouteService.clearRoute();
  }

  protected get targetRoute(): string {
    return this.isLoggedIn ? '/ride-ordering' : '/ride-estimation';
  }

}
