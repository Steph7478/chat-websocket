package com.app.security.auth.cookies;

import org.springframework.http.ResponseCookie;

public class CookieFactory {

    private static final String AUTH_COOKIE = "__Host-AUTH";
    private static final String REFRESH_COOKIE = "__Host-REFRESH";

    public static ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(900)
                .build();
    }

    public static ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(604800)
                .build();
    }

    public static ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }
}
