package com.app.security.auth.jwt.key;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeyProvider {

    private static final Map<String, RSAPrivateCrtKey> PRIVATE_KEYS = new ConcurrentHashMap<>();
    private static final Map<String, RSAPublicKey> PUBLIC_KEYS = new ConcurrentHashMap<>();

    private static String currentKid;

    static {
        rotateKey();
    }

    public static void rotateKey() {
        try {
            RSAPrivateCrtKey privateKey = KeyLoader.generateRSAKey(2048);
            RSAPublicKey publicKey = KeyLoader.derivePublicKey(privateKey);

            String kid = UUID.randomUUID().toString();

            PRIVATE_KEYS.put(kid, privateKey);
            PUBLIC_KEYS.put(kid, publicKey);

            if (PRIVATE_KEYS.size() > 3) {
                cleanOldKeys();
            }

            currentKid = kid;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar chave RSA", e);
        }
    }

    private static void cleanOldKeys() {
        PRIVATE_KEYS.keySet().removeIf(id -> !id.equals(currentKid));
    }

    public static JWSSigner getSigner() {
        return new RSASSASigner(PRIVATE_KEYS.get(currentKid));
    }

    public static JWSVerifier getVerifier(String kid) {
        RSAPublicKey key = PUBLIC_KEYS.get(kid);
        if (key == null) {
            throw new RuntimeException("Token expirado ou Chave (KID) inexistente");
        }
        return new RSASSAVerifier(key);
    }

    public static String getCurrentKid() {
        return currentKid;
    }
}