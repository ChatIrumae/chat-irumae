package com.chatirumae.chatirumae.infra;

import com.chatirumae.chatirumae.core.repository.SessionRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class HashMapSessionRepository implements SessionRepository {
    private Map<String, String> sessionMap = new HashMap<>();

    @Override
    public String createSession(String userId) {
        String session = UUID.randomUUID().toString();
        sessionMap.put(session, userId);
        return session;
    }

    @Override
    public String getUserIdBySession(String session) {
        return sessionMap.get(session);
    }

    @Override
    public void deleteSession(String session) {
        sessionMap.remove(session);
    }

}
