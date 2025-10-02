package com.chatirumae.chatirumae.core.repository;

public interface SessionRepository {
    public String createSession(String userId);
    public String getUserIdBySession(String session);
    public void deleteSession(String session);
}

