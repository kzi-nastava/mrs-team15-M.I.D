import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MapComponent } from '../../../shared/components/map/map';
import { FindingDriverForm } from '../../components/finding-driver-form/finding-driver-form';

@Component({
  selector: 'app-finding-driver',
  imports: [MapComponent, FindingDriverForm],
  templateUrl: './finding-driver.html',
  styleUrl: './finding-driver.css',
})
export class FindingDriver {

  // order data passed via navigation state
  order: any = null;

  constructor(private router: Router) {
    // Try to read navigation state (works on direct navigate calls)
    try {
      const nav = this.router.getCurrentNavigation();
      this.order = nav && (nav.extras as any) && (nav.extras as any).state ? (nav.extras as any).state.order : null;
    } catch (e) {
      this.order = null;
    }
    // Fallback to history.state for cases where getCurrentNavigation is not available
    if (!this.order && typeof history !== 'undefined' && (history as any).state) {
      this.order = (history as any).state.order || null;
    }
  }

}
