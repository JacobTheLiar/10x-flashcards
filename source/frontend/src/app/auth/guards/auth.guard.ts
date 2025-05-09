import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {map, take} from 'rxjs';
import {AuthFacade} from '../store/auth.facade';

export const isAuthenticatedGuard = () => {
  const authFacade = inject(AuthFacade);
  const router = inject(Router);

  return authFacade.isAuthenticated$.pipe(
    take(1),
    map(isAuthenticated => isAuthenticated || router.createUrlTree(['/login']))
  );
};

export const unauthorizedGuard = () => {
  const authFacade = inject(AuthFacade);
  const router = inject(Router);

  return authFacade.isAuthenticated$.pipe(
    take(1),
    map(isAuthenticated => !isAuthenticated || router.createUrlTree(['/generate']))
  );
};
