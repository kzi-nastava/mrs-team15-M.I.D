import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';
import { ProfileInfo } from './pages/profile-info/profile-info';
import { DriverRegistration } from './pages/driver-registration/driver-registration';
import { Login } from './auth/pages/login/login'
import { ForgotPassword } from './auth/pages/forgot-password/forgot-password';
import { ResetPassword } from './auth/pages/reset-password/reset-password';
import { Registration } from './auth/pages/registration/registration';
import { ChangePasswordPage } from './pages/change-password/change-password';
import { DriverHistory } from './pages/driver-history/driver-history';
import { RideDetails } from './pages/ride-details/ride-details';
import {Home} from './home/pages/home/home'
import { RideEstimation } from './ride/pages/ride-estimation/ride-estimation';
import {UpcomingRides} from './ride/pages/upcoming-rides/upcoming-rides'

export const routes: Routes = [
    { path: '', component: Landing },         // Default route (home page)
    { path: 'landing', component: Landing },  // /landing route
    { path: 'login', component: Login},
    { path: 'forgot-password', component: ForgotPassword},
    { path: 'reset-password', component: ResetPassword},
    { path: 'registration', component: Registration},
    { path: 'profile', component: ProfileInfo },
    { path: 'driver-registration', component: DriverRegistration },
    { path: 'change-password', component: ChangePasswordPage },
    { path: 'driver-history', component: DriverHistory },
    { path: 'ride-details/:id', component: RideDetails },
    {path: 'home', component: Home},
    {path: 'ride-estimation', component: RideEstimation},
    {path: 'upcoming-rides', component: UpcomingRides }
];
