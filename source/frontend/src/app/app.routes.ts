import { Routes } from '@angular/router';
import {isAuthenticatedGuard, unauthorizedGuard} from './auth/guards/auth.guard';
import {LoginComponent} from './auth/components/login/login.component';
import {RegisterComponent} from './auth/components/register/register.component';

export const routes: Routes = [
  {path: 'login', component: LoginComponent, canMatch: [unauthorizedGuard]},
  {path: 'register', component: RegisterComponent, canMatch: [unauthorizedGuard]},
  {
    path: '',
    canMatch: [isAuthenticatedGuard],
    children: [
      {
        path: 'generate',
        loadComponent: () => import('./flashcards/components/generate/generate.component').then(c => c.GenerateComponent)
      },
      {
        path: 'flashcards',
        loadComponent: () => import('./flashcards/components/list/flashcards-list.component').then(c => c.FlashcardsListComponent)
      },
      {
        path: 'review',
        loadComponent: () => import('./flashcards/components/review/review-session.component').then(c => c.ReviewSessionComponent)
      },
      {path: '', redirectTo: 'generate', pathMatch: 'full'}
    ]
  },
  {path: '**', redirectTo: 'login'}
];
