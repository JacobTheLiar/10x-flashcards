# Specyfikacja funkcjonalności autoryzacji dla 10x Flashcards

## 1. ARCHITEKTURA INTERFEJSU UŻYTKOWNIKA

### 1.1. Komponenty autoryzacji

#### 1.1.1. Komponent logowania (`LoginComponent`)

- **Ścieżka:** `/login`
- **Technologia:** Komponent standaloneowy Angular 19.2
- Struktura HTML:
    - Formularz logowania z polami email i hasło
    - Przycisk logowania
    - Link do widoku rejestracji
    - Komunikaty błędów
- Funkcjonalność:
    - Obsługa formularza z wykorzystaniem FormBuilder
    - Walidacja pól w czasie rzeczywistym
    - Wyświetlanie błędów walidacji i autentykacji
    - Blokowanie przycisku logowania podczas trwania procesu
    - Automatyczne przekierowanie do widoku generowania fiszek po pomyślnym zalogowaniu

#### 1.1.2. Komponent rejestracji (`RegisterComponent`)

- **Ścieżka:** `/register`
- **Technologia:** Komponent standaloneowy Angular 19.2
- Struktura HTML:
    - Formularz rejestracji z polami: email, hasło, potwierdzenie hasła
    - Przycisk rejestracji
    - Link do widoku logowania
    - Komunikaty błędów
- Funkcjonalność:
    - Obsługa formularza z wykorzystaniem FormBuilder
    - Walidacja pól (format email, złożoność hasła, zgodność haseł)
    - Wyświetlanie błędów walidacji i rejestracji
    - Blokowanie przycisku rejestracji podczas trwania procesu
    - Automatyczne przekierowanie do widoku logowania po pomyślnej rejestracji

### 1.2. Serwisy i modele

#### 1.2.1. Model użytkownika (`User`)

```typescript
export interface User {
  id?: string;
  email?: string;
  createdAt?: string;
}
```

#### 1.2.2. Model żądania autoryzacji

```typescript
export interface LoginRequest {
  email?: string;
  password?: string;
}

export interface RegisterRequest {
  email?: string;
  password?: string;
}

export interface RefreshTokenRequest {
  refreshToken?: string;
}
```

#### 1.2.3. Model odpowiedzi autoryzacji

```typescript
export interface LoginResponse {
  accessToken?: string;
  refreshToken?: string;
  expiresIn?: number;
}

export interface RegisterResponse {
  id?: string;
  email?: string;
  createdAt?: string;
}

export interface RefreshTokenResponse {
  accessToken?: string;
  expiresIn?: number;
}
```

### 1.3. Routing i zabezpieczenie tras

#### 1.3.1. Konfiguracja routingu

```typescript
const routes: Routes = [
  { path: 'login', component: LoginComponent, canMatch: [unauthorizedGuard] },
  { path: 'register', component: RegisterComponent, canMatch: [unauthorizedGuard] },
  {
    path: '',
    canMatch: [isAuthenticatedGuard],
    children: [
      { path: 'generate', component: GenerateComponent },
      { path: 'flashcards', component: FlashcardsListComponent },
      { path: 'review', component: ReviewSessionComponent },
      { path: '', redirectTo: 'generate', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
```

#### 1.3.2. Funkcyjny guard autoryzacji

```typescript
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
```

### 1.4. Walidacja formularzy

#### 1.4.1. Walidatory logowania

```typescript
const loginForm = this.fb.nonNullable.group({
  email: ['', [Validators.required, Validators.email]],
  password: ['', [Validators.required, Validators.minLength(8)]]
});
```

#### 1.4.2. Walidatory rejestracji

```typescript
const registerForm = this.fb.nonNullable.group({
  email: ['', [Validators.required, Validators.email]],
  password: ['', [
    Validators.required,
    Validators.minLength(8)
  ]],
  confirmPassword: ['', Validators.required]
}, { validators: passwordMatchValidator });
```

#### 1.4.3. Własne walidatory

```typescript
export function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password');
  const confirmPassword = control.get('confirmPassword');
  
  if (!password || !confirmPassword) return null;
  
  return password.value === confirmPassword.value ? null : { passwordMismatch: true };
}
```

## 2. IMPLEMENTACJA NGRX

### 2.1. Stan autoryzacji (`auth.state.ts`)

```typescript
export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  tokenExpiration: number | null;
  isLoading: boolean;
  error: string | null;
}

export const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  tokenExpiration: null,
  isLoading: false,
  error: null
};
```

### 2.2. Akcje autoryzacji (`auth.actions.ts`)

```typescript
export const login = createAction(
  '[Auth] Login',
  props<{ email: string; password: string }>()
);

export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ accessToken: string; refreshToken: string; expiresIn: number }>()
);

export const loginFailure = createAction(
  '[Auth] Login Failure',
  props<{ error: string }>()
);

export const register = createAction(
  '[Auth] Register',
  props<{ email: string; password: string }>()
);

export const registerSuccess = createAction(
  '[Auth] Register Success',
  props<{ user: User }>()
);

export const registerFailure = createAction(
  '[Auth] Register Failure',
  props<{ error: string }>()
);

export const refreshToken = createAction(
  '[Auth] Refresh Token'
);

export const refreshTokenSuccess = createAction(
  '[Auth] Refresh Token Success',
  props<{ accessToken: string; expiresIn: number }>()
);

export const refreshTokenFailure = createAction(
  '[Auth] Refresh Token Failure',
  props<{ error: string }>()
);

export const logout = createAction(
  '[Auth] Logout'
);

export const autoLogin = createAction(
  '[Auth] Auto Login'
);
```

### 2.3. Reducer autoryzacji (`auth.reducer.ts`)

```typescript
export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialState,
    on(AuthActions.login, (state) => ({
      ...state,
      isLoading: true,
      error: null
    })),
    on(AuthActions.loginSuccess, (state, { accessToken, refreshToken, expiresIn }) => ({
      ...state,
      accessToken,
      refreshToken,
      tokenExpiration: Date.now() + expiresIn * 1000,
      isLoading: false,
      error: null,
      user: state.user // Użytkownik zostanie zaktualizowany w efekcie login$
    })),
    on(AuthActions.loginFailure, (state, { error }) => ({
      ...state,
      isLoading: false,
      error
    })),
    on(AuthActions.register, (state) => ({
      ...state,
      isLoading: true,
      error: null
    })),
    on(AuthActions.registerSuccess, (state, { user }) => ({
      ...state,
      isLoading: false,
      user
    })),
    on(AuthActions.registerFailure, (state, { error }) => ({
      ...state,
      isLoading: false,
      error
    })),
    on(AuthActions.refreshTokenSuccess, (state, { accessToken, expiresIn }) => ({
      ...state,
      accessToken,
      tokenExpiration: Date.now() + expiresIn * 1000,
      error: null
    })),
    on(AuthActions.refreshTokenFailure, (state) => ({
      ...state,
      user: null,
      accessToken: null,
      refreshToken: null,
      tokenExpiration: null
    })),
      on(AuthActions.setUser, (state, {user}) => ({
          ...state,
          user
      })),
    on(AuthActions.logout, () => initialState)
  )
});

// ... (pozostała część kodu)

export class AuthEffects {
  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      exhaustMap(({ email, password }) =>
        this.authService.login(email, password).pipe(
          map(response => {
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

    // ... (pozostała część kodu)

  autoLogin$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.autoLogin),
      map(() => {
        const authData = this.authService.getAuthData();
        if (!authData) {
          return AuthActions.logout();
        }
        
        const { accessToken, refreshToken, expirationDate } = authData;
        const expiresIn = (expirationDate.getTime() - Date.now()) / 1000;
        
        if (expiresIn <= 0) {
          return AuthActions.refreshToken();
        }

        const user = this.authService.decodeToken(accessToken);
        this.store.dispatch(AuthActions.setUser({
          user: {
            id: user?.sub,
            email: user?.email,
            createdAt: user?.createdAt
          }
        }));
        
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
  ) {}
}
```

### 2.6. Fasada autoryzacji (`auth.facade.ts`)

```typescript
@Injectable({ providedIn: 'root' })
export class AuthFacade {
  // Selektory
  isAuthenticated$ = this.store.select(selectIsAuthenticated);
  user$ = this.store.select(selectUser);
  isLoading$ = this.store.select(selectIsLoading);
  error$ = this.store.select(selectError);
  shouldRefreshToken$ = this.store.select(selectShouldRefreshToken);
  
  constructor(private store: Store) {
    // Automatyczne odświeżanie tokenu
    this.shouldRefreshToken$.pipe(
      filter(shouldRefresh => shouldRefresh),
      debounceTime(1000) // Zapobiega zbyt częstym odświeżeniom
    ).subscribe(() => {
      this.refreshToken();
    });
  }
  
  login(email: string, password: string): void {
    this.store.dispatch(AuthActions.login({ email, password }));
  }
  
  register(email: string, password: string): void {
    this.store.dispatch(AuthActions.register({ email, password }));
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
```

## 3. INTEGRACJA Z API

### 3.1. Serwis autoryzacji (`auth.service.ts`)

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly AUTH_DATA_KEY = 'authData';
  
  constructor(private authController: AuthControllerService) {}
  
  register(email: string, password: string): Observable<RegisterResponse> {
    return this.authController.register({ body: { email, password } });
  }
  
  login(email: string, password: string): Observable<LoginResponse> {
    return this.authController.login({ body: { email, password } });
  }
  
  refreshToken(refreshToken: string): Observable<RefreshTokenResponse> {
    return this.authController.refreshToken({ body: { refreshToken } });
  }
  
  saveAuthData(accessToken: string, refreshToken: string, expiresIn: number): void {
    const expirationDate = new Date(Date.now() + expiresIn * 1000);
    
    const authData = {
      accessToken,
      refreshToken,
      expirationDate
    };
    
    localStorage.setItem(this.AUTH_DATA_KEY, JSON.stringify(authData));
  }
  
  getAuthData(): { accessToken: string; refreshToken: string; expirationDate: Date } | null {
    const authDataStr = localStorage.getItem(this.AUTH_DATA_KEY);
    if (!authDataStr) return null;
    
    const authData = JSON.parse(authDataStr);
    return {
      ...authData,
      expirationDate: new Date(authData.expirationDate)
    };
  }
  
  clearAuthData(): void {
    localStorage.removeItem(this.AUTH_DATA_KEY);
  }
  
  decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(window.atob(base64));
    } catch (e) {
      return null;
    }
  }
}
```

### 3.2. Interceptor HTTP (`auth.interceptor.ts`)

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);
  
  constructor(
    private authFacade: AuthFacade,
    private store: Store
  ) {}
  
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
```

### 3.3. Konfiguracja providera dla interceptora

```typescript
export const authInterceptorProvider = {
  provide: HTTP_INTERCEPTORS,
  useClass: AuthInterceptor,
  multi: true
};
```

## 4. ZABEZPIECZENIA

### 4.1. Inicjalizacja stanu autoryzacji przy starcie aplikacji

```typescript
@Injectable({ providedIn: 'root' })
export class AppInitializerService {
  constructor(private authFacade: AuthFacade) {}
  
  initializeApp(): void {
    this.authFacade.autoLogin();
  }
}

export const appInitializerProvider = {
  provide: APP_INITIALIZER,
  useFactory: (appInitializer: AppInitializerService) => () => {
    return appInitializer.initializeApp();
  },
  deps: [AppInitializerService],
  multi: true
};
```

### 4.2. Bezpieczne przechowywanie tokenów

W celu zwiększenia bezpieczeństwa, tokeny są przechowywane w localStorage, ale z następującymi zabezpieczeniami:

1. **Tokeny krótkoterminowe**:
    - Access token wygasa po 1 godzinie
    - Refresh token wygasa po 7 dniach
2. **Automatyczne odświeżanie**:
    - Access token jest odświeżany automatycznie 5 minut przed wygaśnięciem
    - Wygaśnięcie refresh tokenu prowadzi do wylogowania
3. **Zabezpieczenie przed atakami XSS**:
    - Tokeny są wykorzystywane tylko przez interceptory HTTP
4. **Odporność na CSRF**:
    - Tokeny są przekazywane w nagłówkach HTTP, nie w cookies
    - Każde żądanie zmieniające stan wymaga tokenu JWT

### 4.3. Wsparcie dla Row Level Security

Choć Row Level Security jest implementowane głównie po stronie backendu, frontend zapewnia wsparcie poprzez:

1. **Konsekwentne uwierzytelnianie**:
    - Każde żądanie do API zawiera token JWT w nagłówku
    - Token zawiera identyfikator użytkownika używany przez backend do ustalenia kontekstu użytkownika
2. **Automatyczne uwzględnianie kontekstu użytkownika**:
    - Wszystkie operacje CRUD na fiszkach są wykonywane w kontekście zalogowanego użytkownika
    - Użytkownik widzi tylko własne fiszki dzięki filtrowaniu po stronie serwera

## Podsumowanie

Architektura modułu autoryzacji dla 10x Flashcards jest zgodna z wymaganiami określonymi w dokumencie PRD. Wykorzystuje
komponenty standaloneowe Angular 19.2, NgRx do zarządzania stanem oraz wzorzec fasady do abstrakcji szczegółów
implementacji stanu. System zapewnia bezpieczny dostęp do aplikacji i wspiera mechanizm Row Level Security
zaimplementowany na poziomie bazy danych.