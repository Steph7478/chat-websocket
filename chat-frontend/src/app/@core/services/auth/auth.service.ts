import { Injectable } from "@angular/core";
import { BehaviorSubject, tap } from "rxjs";
import { AuthApi } from "../../api/auth/auth.api";
import { AuthSocket } from "../../api/auth/auth.socket";
import { ChatSocket } from "../../api/chat/chat.socket";

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly SESSION_KEY = 'user_session';

    private loggedIn$ = new BehaviorSubject<boolean>(false);
    readonly isLoggedIn$ = this.loggedIn$.asObservable();

    constructor(
        private authApi: AuthApi,
        private authSocket: AuthSocket,
        private chatSocket: ChatSocket
    ) {
        this.restoreSession();
    }

    private setAuthState(status: boolean) {
        if (status) {
            sessionStorage.setItem(this.SESSION_KEY, 'active');
            this.authSocket.connect();
            this.chatSocket.connect();
        } else {
            sessionStorage.removeItem(this.SESSION_KEY);
            this.authSocket.disconnect();
            this.chatSocket.disconnect();
        }

        this.loggedIn$.next(status);
    }

    private restoreSession() {
        this.authApi.me().subscribe({
            next: () => this.setAuthState(true),
            error: () => this.setAuthState(false)
        });
    }

    login(username: string) {
        return this.authApi.login(username).pipe(
            tap(() => this.setAuthState(true))
        );
    }

    logout() {
        return this.authApi.logout().pipe(
            tap(() => this.setAuthState(false))
        );
    }

    isAuthenticated() {
        return this.loggedIn$.value;
    }
}
