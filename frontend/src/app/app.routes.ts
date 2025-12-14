import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';

export const routes: Routes = [
    { path: '', component: Landing }, // Default route (home page)
  { path: 'landing', component: Landing }, // /landing route
];
