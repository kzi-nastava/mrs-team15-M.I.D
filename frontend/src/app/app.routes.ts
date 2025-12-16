import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';
import { ProfileInfo } from './pages/profile-info/profile-info';
import { Login } from './auth/pages/login/login'
import { ForgotPassword } from './auth/pages/forgot-password/forgot-password';
import { ResetPassword } from './auth/pages/reset-password/reset-password';
import { Registration } from './auth/pages/registration/registration';

export const routes: Routes = [
    { path: '', component: Landing },         // Default route (home page)
    { path: 'landing', component: Landing },  // /landing route
    { path: 'login', component: Login},        
    { path: 'forgot-password', component: ForgotPassword},
    { path: 'reset-password', component: ResetPassword},
    { path: 'registration', component: Registration}
    , { path: 'profile', component: ProfileInfo }
];