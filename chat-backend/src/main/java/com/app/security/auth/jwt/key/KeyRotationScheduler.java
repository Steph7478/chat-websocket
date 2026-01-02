package com.app.security.auth.jwt.key;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@EnableScheduling
public class KeyRotationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeyRotationScheduler.class);

    @Scheduled(fixedRate = 86400000)
    public void scheduleKeyRotation() {
        logger.info("Iniciando rotação automática de chaves RSA...");
        try {
            KeyProvider.rotateKey();
            logger.info("Rotação concluída com sucesso. Novo KID: {}", KeyProvider.getCurrentKid());
        } catch (Exception e) {
            logger.error("Falha crítica na rotação de chaves!", e);
        }
    }
}