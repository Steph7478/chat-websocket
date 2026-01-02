package com.app.security.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityLogger {

    private static final Logger logger = LoggerFactory.getLogger("SECURITY_AUDIT");

    public static void logAnomaly(String type, String userId, String ip, String details) {
        logger.warn("AUDIT_EVENT | TYPE: {} | USER: {} | IP: {} | DETAILS: {}",
                type, userId, ip, details);
    }

    public static void logCritical(String type, String details) {
        logger.error("CRITICAL_SECURITY_ALERT | TYPE: {} | DETAILS: {}", type, details);
    }
}