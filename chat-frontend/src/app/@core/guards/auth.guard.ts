import { CanActivateChildFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateChildFn = () => {
    const router = inject(Router);
    const isLoggedIn = false;

    return isLoggedIn
        ? true
        : router.createUrlTree(['/auth/login']);
};
