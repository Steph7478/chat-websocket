import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { AuthMeDto } from './auth.types';
import { mapAuthUser } from './auth.mapper';

@Injectable({ providedIn: 'root' })
export class AuthApi {
    constructor(private http: HttpClient) { }

    login(username: string) {
        return this.http.post(
            '/api/auth/login',
            null,
            {
                params: { username },
                withCredentials: true
            }
        );
    }

    me() {
        return this.http
            .get<AuthMeDto>('/auth/me', {
                withCredentials: true
            })
            .pipe(map(mapAuthUser));
    }

    logout() {
        return this.http.post(
            '/api/auth/logout',
            null,
            { withCredentials: true }
        );
    }
}
