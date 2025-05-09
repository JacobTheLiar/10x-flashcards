import {Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {Router} from '@angular/router';
import {catchError, exhaustMap, map, of, tap} from 'rxjs';
import {User} from '../models/user.model';
import {AuthService} from '../services/auth.service';
import * as AuthActions from './auth.actions';
import {selectRefreshToken} from './auth.reducer';

@Injectable()
export class AuthEffects {
  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      exhaustMap(({email, password}) =>
        this.authService.login(email, password).pipe(
          map(response => {
            // Zapisz dane uwierzytelniania w localStorage
            this.authService.saveAuthData(
              response.accessToken || '',
              response.refreshToken || '',
              response.expiresIn || 3600
            );
            // Dekoduj token, aby uzyskać informacje o użytkowniku
            const user = this.authService.decodeToken(response.accessToken || '');
            if (user) {
              this.store.dispatch(AuthActions.setUser({
                user: {
                  id: user.sub,
                  email: user.email,
                  createdAt: user.createdAt
                }
              }));
            }
            return AuthActions.loginSuccess({
              accessToken: response.accessToken || '',
              refreshToken: response.refreshToken || '',
              expiresIn: response.expiresIn || 3600
            });
          }),
          catchError(error => of(AuthActions.loginFailure({
            error: error.error?.message || 'Błąd logowania'
          })))
        )
      )
    )
  );

  loginSuccess$ = createEffect(() =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        tap(() => {
          this.router.navigate(['/generate']);
        })
      ),
    {dispatch: false}
  );

  register$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.register),
      exhaustMap(({email, password}) =>
        this.authService.register(email, password).pipe(
          map(user => AuthActions.registerSuccess({user})),
          catchError(error => of(AuthActions.registerFailure({
            error: error.error?.message || 'Błąd rejestracji'
          })))
        )
      )
    )
  );

  registerSuccess$ = createEffect(() =>
      this.actions$.pipe(
        ofType(AuthActions.registerSuccess),
        tap(() => {
          this.router.navigate(['/login']);
        })
      ),
    {dispatch: false}
  );

  refreshToken$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.refreshToken),
      exhaustMap(() => this.store.select(selectRefreshToken).pipe(
        exhaustMap(refreshToken => {
          if (!refreshToken) {
            return of(AuthActions.logout());
          }

          return this.authService.refreshToken(refreshToken).pipe(
            map(response => {
              const authData = this.authService.getAuthData();
              if (authData && response.accessToken) {
                this.authService.saveAuthData(
                  response.accessToken,
                  authData.refreshToken,
                  response.expiresIn || 3600
                );
              }

              return AuthActions.refreshTokenSuccess({
                accessToken: response.accessToken || '',
                expiresIn: response.expiresIn || 3600
              });
            }),
            catchError(() => of(AuthActions.refreshTokenFailure({error: 'Błąd odświeżania tokenu'})))
          );
        })
      ))
    )
  );

  logout$ = createEffect(() =>
      this.actions$.pipe(
        ofType(AuthActions.logout),
        tap(() => {
          this.authService.clearAuthData();
          this.router.navigate(['/login']);
        })
      ),
    {dispatch: false}
  );

  autoLogin$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.autoLogin),
      map(() => {
        const authData = this.authService.getAuthData();
        if (!authData) {
          return AuthActions.logout();
        }

        const {accessToken, refreshToken, expirationDate} = authData;
        const expiresIn = (expirationDate.getTime() - Date.now()) / 1000;

        if (expiresIn <= 0) {
          return AuthActions.refreshToken();
        }

        const user = this.authService.decodeToken(accessToken);
        if (user) {
          this.store.dispatch(AuthActions.setUser({
            user: {
              id: user.sub,
              email: user.email,
              createdAt: user.createdAt
            }
          }));
        }

        return AuthActions.loginSuccess({
          accessToken,
          refreshToken,
          expiresIn
        });
      })
    )
  );

  constructor(
    private actions$: Actions,
    private authService: AuthService,
    private store: Store,
    private router: Router
  ) {
  }
}
