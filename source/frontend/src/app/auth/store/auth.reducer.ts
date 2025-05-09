import {createFeature, createReducer, on} from '@ngrx/store';
import {User} from '../models/user.model';
import * as AuthActions from './auth.actions';

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

export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialState,
    on(AuthActions.login, (state) => ({
      ...state,
      isLoading: true,
      error: null
    })),
    on(AuthActions.loginSuccess, (state, {accessToken, refreshToken, expiresIn}) => ({
      ...state,
      accessToken,
      refreshToken,
      tokenExpiration: Date.now() + expiresIn * 1000,
      isLoading: false,
      error: null
    })),
    on(AuthActions.loginFailure, (state, {error}) => ({
      ...state,
      isLoading: false,
      error
    })),
    on(AuthActions.register, (state) => ({
      ...state,
      isLoading: true,
      error: null
    })),
    on(AuthActions.registerSuccess, (state, {user}) => ({
      ...state,
      isLoading: false,
      user
    })),
    on(AuthActions.registerFailure, (state, {error}) => ({
      ...state,
      isLoading: false,
      error
    })),
    on(AuthActions.refreshTokenSuccess, (state, {accessToken, expiresIn}) => ({
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

export const {
  name,
  reducer,
  selectAuthState,
  selectUser,
  selectAccessToken,
  selectRefreshToken,
  selectTokenExpiration,
  selectIsLoading,
  selectError
} = authFeature;
