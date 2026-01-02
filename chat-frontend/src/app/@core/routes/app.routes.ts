import { Routes } from '@angular/router';
import { MainLayoutComponent } from '../layout/main-layout.component';

export const routes: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            {
                path: '',
                loadChildren: () =>
                    import('../../features/home')
                        .then(m => m.HOME_ROUTES),
            },
        ],
    },
];
