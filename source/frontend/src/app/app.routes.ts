import { Routes } from '@angular/router';
import {isAuthenticatedGuard} from './auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./auth/components/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/components/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: 'generate',
    loadComponent: () => import('./flashcards/components/generate/generate.component').then(m => m.GenerateComponent),
    canActivate: [isAuthenticatedGuard],
  },
  {
    path: 'flashcards',
    loadComponent: () => import('./flashcards/components/list/flashcards-list.component').then(m => m.FlashcardsListComponent),
    canActivate: [isAuthenticatedGuard],
  },
  {
    path: '',
    redirectTo: 'generate',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'generate',
  }
];
