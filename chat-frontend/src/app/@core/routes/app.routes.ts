import { Routes } from '@angular/router';
import { CORE_ROUTES } from './router';
import { AUTH_ROUTES } from '../../features/auth/router/auth.routes';

export const APP_ROUTES: Routes = [
    ...AUTH_ROUTES,
    ...CORE_ROUTES
];
