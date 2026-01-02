package com.app.security.auth.jwt.key;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class KeyLoader {

    public static RSAPrivateCrtKey loadPrivateKey(String resourcePath) throws Exception {
        InputStream is = KeyLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (is == null)
            throw new RuntimeException("Private key not found");

        String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        RSAPrivateKey pk = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);

        if (!(pk instanceof RSAPrivateCrtKey))
            throw new RuntimeException("Private key must be RSAPrivateCrtKey");

        return (RSAPrivateCrtKey) pk;
    }

    public static RSAPublicKey derivePublicKey(RSAPrivateCrtKey privateKey) throws Exception {
        RSAPublicKeySpec spec = new RSAPublicKeySpec(
                privateKey.getModulus(),
                privateKey.getPublicExponent());
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static RSAPrivateCrtKey generateRSAKey(int bits) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(bits);
        KeyPair pair = gen.generateKeyPair();
        return (RSAPrivateCrtKey) pair.getPrivate();
    }
}
