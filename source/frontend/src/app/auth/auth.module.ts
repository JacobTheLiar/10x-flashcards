import {APP_INITIALIZER, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import * as fromAuth from './store/auth.reducer';
import {AuthEffects} from './store/auth.effects';
import {AuthInterceptor} from './services/auth.interceptor';
import {AppInitializerService} from './services/app-initializer.service';

export function initializeApp(appInitializer: AppInitializerService): () => void {
  return () => appInitializer.initializeApp();
}

@NgModule({
  imports: [
    HttpClientModule,
    StoreModule.forFeature(fromAuth.authFeature),
    EffectsModule.forFeature([AuthEffects]),
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AppInitializerService],
      multi: true
    }
  ]
})
export class AuthModule {
}
