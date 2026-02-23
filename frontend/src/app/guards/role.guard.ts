import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const token = localStorage.getItem('jwtToken');
    const role = localStorage.getItem('role');

    // Not authenticated -> send to login
    if (!token) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // If route has role restrictions, check them
    const allowedRoles: string[] | undefined = route.data && route.data['roles'];
    if (allowedRoles && allowedRoles.length > 0) {
      if (!role) {
        this.router.navigate(['/unauthorized']);
        return false;
      }

      // roles in app are stored as e.g. 'ADMIN', 'DRIVER', 'USER'
      if (allowedRoles.includes(role)) {
        return true;
      }

      // Authenticated but not authorized
      this.router.navigate(['/unauthorized']);
      return false;
    }

    // No role restrictions and user is authenticated
    return true;
  }
}
