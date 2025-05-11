import {APP_INITIALIZER, ApplicationConfig, importProvidersFrom} from '@angular/core';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import { routes } from './app.routes';
import {provideHttpClient, withInterceptorsFromDi, withFetch} from '@angular/common/http';
import {provideStoreDevtools} from '@ngrx/store-devtools';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {AuthModule} from './auth/auth.module';
import {provideAnimations} from '@angular/platform-browser/animations';
import {environment} from '../environments/environment';
import {ApiConfiguration} from "@api/api-configuration";
import {flashcardsFeature} from "./flashcards/store/flashcards.reducer";
import {FlashcardsEffects} from "./flashcards/store/flashcards.effects";

export function apiConfigFactory(config: ApiConfiguration): Function {
  return () => {
    config.rootUrl = '';
    console.log('apiConfigFactory: setting rootUrl to empty string');
    return config;
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptorsFromDi(), withFetch()),
    importProvidersFrom(
      StoreModule.forRoot({}),
      StoreModule.forFeature(flashcardsFeature),
      EffectsModule.forRoot([]),
      EffectsModule.forFeature([FlashcardsEffects]),
      AuthModule,
    ),
    provideStoreDevtools({
      maxAge: 25,
      logOnly: environment.production
    }),
    provideAnimations(),
    {
      provide: APP_INITIALIZER,
      useFactory: apiConfigFactory,
      deps: [ApiConfiguration],
      multi: true
    }
  ]
};
