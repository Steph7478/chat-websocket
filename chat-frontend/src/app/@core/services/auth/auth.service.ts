import { Injectable } from "@angular/core";
import { BehaviorSubject, tap } from "rxjs";
import { AuthApi } from "../../api/auth/auth.api";

@Injectable({ providedIn: 'root' })
export class AuthService {

    private loggedInSubject = new BehaviorSubject<boolean>(false);
    readonly isLoggedIn$ = this.loggedInSubject.asObservable();

    private usernameSubject = new BehaviorSubject<string | null>(null);
    readonly username$ = this.usernameSubject.asObservable();

    constructor(private authApi: AuthApi) {
        this.restoreSession();
    }

    login(username: string) {
        return this.authApi.login(username).pipe(
            tap(() => {
                this.loggedInSubject.next(true);
                this.usernameSubject.next(username);
            })
        );
    }

    logout() {
        this.loggedInSubject.next(false);
        this.usernameSubject.next(null);
        return this.authApi.logout();
    }

    isAuthenticated(): boolean {
        return this.loggedInSubject.value;
    }

    getUsername(): string | null {
        return this.usernameSubject.value;
    }

    private restoreSession() {
        this.authApi.me().subscribe({
            next: (user) => {
                this.loggedInSubject.next(true);
                this.usernameSubject.next(user.username);
            },
            error: () => {
                this.loggedInSubject.next(false);
                this.usernameSubject.next(null);
            }
        });
    }
}
