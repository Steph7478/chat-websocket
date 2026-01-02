package com.app.security.auth.jwt.key;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

public final class SignerProvider {

    private static final RSAPrivateCrtKey PRIVATE_KEY;
    private static final RSAPublicKey PUBLIC_KEY;

    static {
        try {
            PRIVATE_KEY = KeyLoader.loadPrivateKey("keys/private.pem");
            PUBLIC_KEY = KeyLoader.derivePublicKey(PRIVATE_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA keys", e);
        }
    }

    private SignerProvider() {
    }

    public static JWSSigner signer() {
        return new RSASSASigner(PRIVATE_KEY);
    }

    public static JWSVerifier verifier() {
        return new RSASSAVerifier(PUBLIC_KEY);
    }
}
