import { Routes } from '@angular/router';
import { MainLayoutComponent } from '../layout/main-layout.component';
import { authGuard } from '../guards/auth.guard';
import { AUTH_ROUTES } from '../../features/auth/router/auth.routes';
import { HOME_ROUTES } from '../../features/home/router/home.routes';

export const APP_ROUTES: Routes = [
  ...AUTH_ROUTES,

  {
    path: '',
    component: MainLayoutComponent,
    canActivateChild: [authGuard],
    children: [...HOME_ROUTES],
  },

  { path: '**', redirectTo: '' },
];
