import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';
import { DriverHistory } from './pages/driver-history/driver-history';

export const routes: Routes = [
    { path: '', component: Landing }, // Default route (home page)
  { path: 'landing', component: Landing },
  { path: 'driver-history', component: DriverHistory },
];
