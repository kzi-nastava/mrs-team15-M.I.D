import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';
import { Login } from './auth/pages/login/login'

export const routes: Routes = [
    { path: '', component: Landing },         // Default route (home page)
    { path: 'landing', component: Landing },  // /landing route
    { path: 'login', component: Login}        // login page route
];