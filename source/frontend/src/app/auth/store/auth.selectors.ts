import {createSelector} from '@ngrx/store';
import {selectAccessToken, selectRefreshToken, selectTokenExpiration, selectUser} from './auth.reducer';

export const selectIsAuthenticated = createSelector(
  selectAccessToken,
  (accessToken): boolean => !!accessToken
);

export const selectShouldRefreshToken = createSelector(
  selectAccessToken,
  selectTokenExpiration,
  (accessToken, tokenExpiration): boolean => {
    if (!accessToken || !tokenExpiration) {
      return false;
    }
    // Odśwież token 5 minut przed jego wygaśnięciem
    const refreshThreshold = 5 * 60 * 1000; // 5 minut w milisekundach
    return tokenExpiration - Date.now() < refreshThreshold;
  }
);
