import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, Observable, throwError, EMPTY} from 'rxjs';
import {catchError, filter, switchMap, take, skipWhile, tap} from 'rxjs/operators';
import {Store} from '@ngrx/store';
import {AuthFacade} from '../store/auth.facade';
import {selectAccessToken, selectRefreshToken} from '../store/auth.reducer';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(
    private authFacade: AuthFacade,
    private store: Store
  ) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log('Intercepting request to:', request.url);

    if (this.isAuthRequest(request)) {
      console.log('Auth request, skipping token');
      return next.handle(request);
    }

    return this.store.select(selectAccessToken).pipe(
      take(1),
      tap(token => console.log('Current token state:', token ? 'Present' : 'Missing')),
      switchMap(token => {
        if (!token) {
          console.log('No token available, proceeding without authentication');
          return next.handle(request);
        }

        const authRequest = this.addToken(request, token);
        console.log('Added token to request');

        return next.handle(authRequest).pipe(
          catchError(error => {
            if (error instanceof HttpErrorResponse) {
              console.error('HTTP Error:', error.status, error.message);

              if (error.status === 401) {
                console.log('401 Unauthorized - Attempting token refresh');
                return this.handle401Error(request, next);
              }
            }
            return throwError(() => error);
          })
        );
      })
    );
  }

  private isAuthRequest(request: HttpRequest<any>): boolean {
    const url = request.url.toLowerCase();
    return url.includes('/api/auth/login') ||
      url.includes('/api/auth/register') ||
      url.includes('/api/auth/refresh-token');
  }

  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);
      console.log('Starting token refresh');

      return this.store.select(selectRefreshToken).pipe(
        take(1),
        tap(token => console.log('Refresh token state:', token ? 'Present' : 'Missing')),
        switchMap(refreshToken => {
          if (!refreshToken) {
            console.log('No refresh token, logging out');
            this.isRefreshing = false;
            this.authFacade.logout();
            return EMPTY;
          }

          console.log('Attempting to refresh token');
          this.authFacade.refreshToken();

          return this.store.select(selectAccessToken).pipe(
            tap(token => console.log('Waiting for new token...', token ? 'Received' : 'Still waiting')),
            skipWhile(token => !token),
            take(1),
            switchMap(newToken => {
              console.log('New token received, resuming request');
              this.isRefreshing = false;
              this.refreshTokenSubject.next(newToken);
              return next.handle(this.addToken(request, newToken!));
            }),
            catchError(error => {
              console.error('Error refreshing token:', error);
              this.isRefreshing = false;
              this.authFacade.logout();
              return EMPTY;
            })
          );
        })
      );
    } else {
      console.log('Already refreshing, waiting for completion');
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => next.handle(this.addToken(request, token!)))
      );
    }
  }
}
