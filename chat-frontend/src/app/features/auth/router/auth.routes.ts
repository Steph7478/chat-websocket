import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
    {
        path: 'auth',
        loadComponent: () =>
            import('../pages/auth.page')
                .then(m => m.AuthPage),

        children: [
            {
                path: 'login',
                loadComponent: () =>
                    import('../components/login/login.component')
                        .then(m => m.LoginPage)
            },
            {
                path: '',
                redirectTo: 'login',
                pathMatch: 'full'
            }
        ]
    }
];
