package com.app.security.session;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class Fingerprint {

    public static Mono<String> generate(String userId, String userAgent, String ip) {
        return Mono.fromCallable(() -> {
            char[] data = (userId + "|" + userAgent + "|" + ip).toCharArray();
            Argon2 argon2 = Argon2Factory.create();
            String hash = argon2.hash(3, 65536, 1, data);
            argon2.wipeArray(data);
            return hash;

        }).subscribeOn(Schedulers.boundedElastic());
    }
}
