import {Injectable} from '@angular/core';
import {AuthFacade} from '../store/auth.facade';

@Injectable({
  providedIn: 'root'
})
export class AppInitializerService {
  constructor(private readonly authFacade: AuthFacade) {
  }

  initializeApp(): void {
    this.authFacade.autoLogin();
  }
}
