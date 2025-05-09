import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, Observable, throwError, EMPTY} from 'rxjs';
import {catchError, filter, switchMap, take, skipWhile} from 'rxjs/operators';
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
    // Nie dodawaj tokenu do endpointów autoryzacji
    if (this.isAuthRequest(request)) {
      return next.handle(request);
    }

    // Pobierz aktualny token
    return this.store.select(selectAccessToken).pipe(
      take(1),
      switchMap(token => {
        if (!token) {
          return next.handle(request);
        }

        // Dodaj token do nagłówka
        const authRequest = this.addToken(request, token);

        // Obsłuż odpowiedź i potencjalne błędy autoryzacji
        return next.handle(authRequest).pipe(
          catchError(error => {
            if (error instanceof HttpErrorResponse && error.status === 401) {
              return this.handle401Error(request, next);
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

      return this.store.select(selectRefreshToken).pipe(
        take(1),
        switchMap(refreshToken => {
          if (!refreshToken) {
            this.isRefreshing = false;
            this.authFacade.logout();
            return EMPTY;
          }

          this.authFacade.refreshToken();

          return this.store.select(selectAccessToken).pipe(
            skipWhile(token => !token),
            take(1),
            switchMap(newToken => {
              this.isRefreshing = false;
              this.refreshTokenSubject.next(newToken);
              return next.handle(this.addToken(request, newToken!));
            }),
            catchError(() => {
              this.isRefreshing = false;
              this.authFacade.logout();
              return EMPTY;
            })
          );
        })
      );
    } else {
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => next.handle(this.addToken(request, token!)))
      );
    }
  }
}
