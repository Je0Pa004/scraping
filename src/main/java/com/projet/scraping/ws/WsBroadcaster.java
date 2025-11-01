package com.projet.scraping.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WsBroadcaster {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    void add(WebSocketSession session) {
        sessions.add(session);
    }

    void remove(WebSocketSession session) {
        sessions.remove(session);
    }

    public void broadcast(Object payload) {
        try {
            String json = (payload instanceof String) ? (String) payload : mapper.writeValueAsString(payload);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(new TextMessage(json));
                    } catch (IOException ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }
}
