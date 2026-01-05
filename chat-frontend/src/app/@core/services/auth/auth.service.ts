import { Injectable } from "@angular/core";
import { BehaviorSubject, tap } from "rxjs";
import { AuthApi } from "../../api/auth/auth.api";

@Injectable({ providedIn: 'root' })
export class AuthService {

    private loggedInSubject = new BehaviorSubject<boolean>(false);
    readonly isLoggedIn$ = this.loggedInSubject.asObservable();

    constructor(private authApi: AuthApi) {
        this.restoreSession();
    }

    login(username: string) {
        return this.authApi.login(username).pipe(
            tap(() => this.loggedInSubject.next(true))
        );
    }

    logout() {
        this.loggedInSubject.next(false);
        return this.authApi.logout();
    }

    isAuthenticated(): boolean {
        return this.loggedInSubject.value;
    }

    private restoreSession() {
        this.authApi.me().subscribe({
            next: () => this.loggedInSubject.next(true),
            error: () => this.loggedInSubject.next(false)
        });
    }
}
