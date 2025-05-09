import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable, debounceTime, filter} from 'rxjs';
import * as AuthActions from './auth.actions';
import {selectError, selectIsLoading, selectUser} from './auth.reducer';
import {selectIsAuthenticated, selectShouldRefreshToken} from './auth.selectors';
import {User} from '../models/user.model';

@Injectable({providedIn: 'root'})
export class AuthFacade {
  readonly isAuthenticated$: Observable<boolean>;
  readonly user$: Observable<User | null>;
  readonly isLoading$: Observable<boolean>;
  readonly error$: Observable<string | null>;
  readonly shouldRefreshToken$: Observable<boolean>;

  constructor(private store: Store) {
    this.isAuthenticated$ = this.store.select(selectIsAuthenticated);
    this.user$ = this.store.select(selectUser);
    this.isLoading$ = this.store.select(selectIsLoading);
    this.error$ = this.store.select(selectError);
    this.shouldRefreshToken$ = this.store.select(selectShouldRefreshToken);

    this.shouldRefreshToken$.pipe(
      filter(shouldRefresh => shouldRefresh),
      debounceTime(1000)
    ).subscribe(() => {
      this.refreshToken();
    });
  }

  login(email: string, password: string): void {
    this.store.dispatch(AuthActions.login({email, password}));
  }

  register(email: string, password: string): void {
    this.store.dispatch(AuthActions.register({email, password}));
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }

  refreshToken(): void {
    this.store.dispatch(AuthActions.refreshToken());
  }

  autoLogin(): void {
    this.store.dispatch(AuthActions.autoLogin());
  }
}
