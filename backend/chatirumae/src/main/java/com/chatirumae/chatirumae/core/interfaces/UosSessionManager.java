package com.chatirumae.chatirumae.core.interfaces;


import com.chatirumae.chatirumae.core.model.UosSession;

public interface UosSessionManager {
    public UosSession createUosSession(String portalId, String portalPassword);

    public boolean isSessionValid(UosSession session);
}
