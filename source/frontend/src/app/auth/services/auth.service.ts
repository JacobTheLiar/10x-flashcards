import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

// Interfejsy odpowiadające strukturom z API
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken?: string;
  refreshToken?: string;
  expiresIn?: number;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface RegisterResponse {
  id?: string;
  email?: string;
  createdAt?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken?: string;
  expiresIn?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly AUTH_DATA_KEY = 'authData';
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  register(email: string, password: string): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/api/auth/register`, {email, password});
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/api/auth/login`, {email, password});
  }

  refreshToken(refreshToken: string): Observable<RefreshTokenResponse> {
    return this.http.post<RefreshTokenResponse>(`${this.apiUrl}/api/auth/refresh-token`, {refreshToken});
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
      console.error('Błąd dekodowania tokenu JWT:', e);
      return null;
    }
  }
}
