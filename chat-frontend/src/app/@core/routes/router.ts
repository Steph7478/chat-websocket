import { Routes } from '@angular/router';
import { MainLayoutComponent } from '../layout/main-layout.component';
import { authGuard } from '../guards/auth.guard';

export const CORE_ROUTES: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivateChild: [authGuard],
    children: [
      {
        path: '',
        loadChildren: () =>
          import('../../features/home/router/home.routes')
            .then(m => m.HOME_ROUTES),
      }
    ],
  },
];
