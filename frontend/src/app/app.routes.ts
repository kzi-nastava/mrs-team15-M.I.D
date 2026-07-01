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
import { PassangerReport } from './reports/pages/passanger-report/passanger-report';
import { DriverReport } from './reports/pages/driver-report/driver-report';
import { UpcomingRides } from './ride/pages/upcoming-rides/upcoming-rides';
import { AdminHistory } from './history/pages/admin-history/admin-history';
import { RoleGuard } from './guards/role.guard';
import { AdminUsers } from './admin/pages/users/users';
import { HistoryRideDetails } from './history/pages/history-ride-details/history-ride-details';
import { CurrentRide } from './ride/pages/current-ride/current-ride';
import { StartRide } from './ride/pages/start-ride/start-ride';
import { RideOrdering } from './ride/pages/ride-ordering/ride-ordering';
import { FindingDriver } from './ride/pages/finding-driver/finding-driver';
import { ActivationPage } from './auth/pages/activation-page/activation-page';
import { AdminHistoryOverview } from './history/pages/admin-history-overview/admin-history-overview';
import { PricingManagement } from './admin/pages/pricing-management/pricing-management';
import { AdminReport } from './reports/pages/admin-report/admin-report';
import { ActiveRides } from './admin/pages/active-rides/active-rides';
import { AdminChats } from './admin/pages/admin-chats/admin-chats';
import { AdminChatDetail } from './admin/pages/admin-chat-detail/admin-chat-detail';
import { UserChat } from './pages/user-chat/user-chat';
import { PanicAlerts } from './admin/pages/panic-alerts/panic-alerts';
import { Unauthorized } from './pages/unauthorized/unauthorized';

export const routes: Routes = [
    { path: '', component: Home },         // Default route (home page)
    { path: 'landing', component: Landing },  // /landing route
    { path: 'login', component: Login},
    { path: 'forgot-password', component: ForgotPassword},
    { path: 'reset-password/:token', component: ResetPassword},
    { path: 'driver-activation/:token', component: DriverActivation},
    { path: 'registration', component: Registration},
    { path: 'profile', component: ProfileInfo, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'USER', 'DRIVER'] } },
    { path: 'change-request', component: ChangeRequest, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'driver-requests', component: DriverRequestsPage, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'driver-registration', component: DriverRegistration },
    { path: 'change-password', component: ChangePasswordPage },
    { path: 'driver-history', component: DriverHistory, canActivate: [RoleGuard], data: { roles: ['DRIVER'] } },
    { path: 'driver-rides', component: DriverAppointedRides, canActivate: [RoleGuard], data: { roles: ['DRIVER'] } },
    { path: 'ride-details/:id', component: RideDetails, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'USER', 'DRIVER'] } },
    { path: 'user-history', component: UserHistory, canActivate: [RoleGuard], data: { roles: ['USER'] } },
    { path: 'passanger-report', component: PassangerReport, canActivate: [RoleGuard], data: { roles: ['USER'] } },
    { path: 'driver-report', component: DriverReport, canActivate: [RoleGuard], data: { roles: ['DRIVER'] } },
    { path: 'home', component: Home },
    { path: 'ride-estimation', component: RideEstimation },
    { path: 'rating/:id', component: Rating, canActivate: [RoleGuard], data: { roles: ['AUSER'] } },
    { path: 'upcoming-rides', component: UpcomingRides, canActivate: [RoleGuard], data: { roles: ['USER', 'DRIVER'] } },
    { path: 'admin-history/:id', component: AdminHistory, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'admin-users', component: AdminUsers, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'history-ride-details/:id', component: HistoryRideDetails },
    { path: 'current-ride', component: CurrentRide, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'USER', 'DRIVER'] } },
    { path: 'start-ride', component: StartRide, canActivate: [RoleGuard], data: { roles: ['DRIVER'] } },
    { path: 'finding-driver', component: FindingDriver, canActivate: [RoleGuard], data: { roles: ['USER'] } },
    { path: 'ride-ordering', component: RideOrdering, canActivate: [RoleGuard], data: { roles: ['USER'] } },
    { path: 'admin-pricing', component: PricingManagement, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'admin-report', component: AdminReport, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'admin-history-overview', component: AdminHistoryOverview, canActivate: [RoleGuard], data: { roles: ['ADMIN'] }},
    { path: 'admin-active-rides', component: ActiveRides, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'admin-chats', component: AdminChats, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'admin-chat/:id', component: AdminChatDetail, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'user-chat', component: UserChat, canActivate: [RoleGuard], data: { roles: ['USER'] } },
    { path: 'activate/:token', component: ActivationPage },
    { path: 'unauthorized', component: Unauthorized },
    { path: 'panic-alerts', component: PanicAlerts, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },

];
