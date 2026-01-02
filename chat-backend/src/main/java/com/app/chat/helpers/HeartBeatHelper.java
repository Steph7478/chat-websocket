package com.app.chat.helpers;

import org.springframework.stereotype.Component;

@Component
public class HeartBeatHelper {

    public boolean isHeartbeat(String payload) {
        return payload.contains("\"type\":\"HEARTBEAT\"");
    }
}
