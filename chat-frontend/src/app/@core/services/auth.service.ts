import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private loggedIn = new BehaviorSubject<boolean>(false);

    isLoggedIn$ = this.loggedIn.asObservable();

    constructor() {
        const hasSession = localStorage.getItem('user_session') === 'active';
        if (hasSession) this.loggedIn.next(true);
    }

    login() {
        this.loggedIn.next(true);
        localStorage.setItem('user_session', 'active');
    }

    logout() {
        this.loggedIn.next(false);
        localStorage.removeItem('user_session');
    }

    isAuthenticated(): boolean {
        return this.loggedIn.value;
    }
}