package com.chatirumae.chatirumae.core.interfaces;

import com.chatirumae.chatirumae.core.model.UosSession;
import com.chatirumae.chatirumae.core.model.UserBasicInfo;

public interface UosApi {
    // 세션을 통해 유저의 기본 정보를 반환 (이를 통해 학번을 획득)
    public UserBasicInfo getUserBasicInfo(UosSession session);
}
