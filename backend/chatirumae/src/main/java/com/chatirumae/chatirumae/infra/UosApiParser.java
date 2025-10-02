package com.chatirumae.chatirumae.infra;

import com.chatirumae.chatirumae.core.model.UserBasicInfo;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class UosApiParser {

    public UserBasicInfo parseUserBasicInfo(String response) {
        JSONObject obj = new JSONObject(response);
        JSONObject userInfo = obj.getJSONObject("dmUserInfo");
        String name = userInfo.getString("USER_NM");
        String studentId = userInfo.getString("USER_ID");
        return new UserBasicInfo(name, studentId);
    }
}
