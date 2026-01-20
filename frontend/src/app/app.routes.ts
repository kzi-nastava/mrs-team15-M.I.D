import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';
import { ProfileInfo } from './pages/profile-info/profile-info';
import { DriverRegistration } from './pages/driver-registration/driver-registration';
import { Login } from './auth/pages/login/login'
import { ForgotPassword } from './auth/pages/forgot-password/forgot-password';
import { ResetPassword } from './auth/pages/reset-password/reset-password';
import { DriverActivation } from './auth/pages/driver-activation/driver-activation';
import { Registration } from './auth/pages/registration/registration';
import { ChangePasswordPage } from './pages/change-password/change-password';
import { ChangeRequest } from './driver/pages/change-request/change-request';
import { DriverRequestsPage } from './driver/pages/driver-requests/driver-requests';
import { DriverHistory } from './history/pages/driver-history/driver-history';
import { DriverAppointedRides } from './ride/pages/driver-appointed-rides/driver-appointed-rides';
import { RideDetails } from './history/pages/ride-details/ride-details';
import { Home } from './home/pages/home/home';
import { RideEstimation } from './ride/pages/ride-estimation/ride-estimation';
import { Rating } from './ride/pages/rating/rating';
import { UserHistory } from './history/pages/user-history/user-history';
import { UpcomingRides } from './ride/pages/upcoming-rides/upcoming-rides';
import { AdminHistory } from './history/pages/admin-history/admin-history';
import { HistoryRideDetails } from './history/pages/history-ride-details/history-ride-details';
import { CurrentRide } from './ride/pages/current-ride/current-ride';
import { StartRide } from './ride/pages/start-ride/start-ride';
import { RideOrdering } from './ride/pages/ride-ordering/ride-ordering';
import { FindingDriver } from './ride/pages/finding-driver/finding-driver';
import { ActivationPage } from './auth/pages/activation-page/activation-page';

export const routes: Routes = [
    { path: '', component: Landing },         // Default route (home page)
    { path: 'landing', component: Landing },  // /landing route
    { path: 'login', component: Login},
    { path: 'forgot-password', component: ForgotPassword},
    { path: 'reset-password/:token', component: ResetPassword},
    { path: 'driver-activation/:token', component: DriverActivation},
    { path: 'registration', component: Registration},
    { path: 'profile', component: ProfileInfo },
    { path: 'change-request', component: ChangeRequest },
    { path: 'driver-requests', component: DriverRequestsPage },
    { path: 'driver-registration', component: DriverRegistration },
    { path: 'change-password', component: ChangePasswordPage },
    { path: 'driver-history', component: DriverHistory },
    { path: 'driver-rides', component: DriverAppointedRides },
    { path: 'ride-details/:id', component: RideDetails },
    { path: 'user-history', component: UserHistory },
    { path: 'home', component: Home },
    { path: 'ride-estimation', component: RideEstimation },
    { path: 'rating', component: Rating },
    { path: 'upcoming-rides', component: UpcomingRides },
    { path: 'admin-history', component: AdminHistory },
    { path: 'history-ride-details/:id', component: HistoryRideDetails },
    { path: 'current-ride', component: CurrentRide },
    { path: 'start-ride', component: StartRide },
    { path: 'finding-driver', component: FindingDriver },
    { path: 'ride-ordering', component: RideOrdering },
    { path: 'activate/:token', component: ActivationPage }
];