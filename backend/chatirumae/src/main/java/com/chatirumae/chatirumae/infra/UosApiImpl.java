package com.chatirumae.chatirumae.infra;

import com.chatirumae.chatirumae.core.interfaces.UosApi;
import com.chatirumae.chatirumae.core.model.UosSession;
import com.chatirumae.chatirumae.core.model.UserBasicInfo;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

@Component
public class UosApiImpl implements UosApi {

    private final UosApiParser parser;

    private static final String springSemesterCode = "CCMN031.10";
    private static final String summerSemesterCode = "CCMN031.11";
    private static final String fallSemesterCode = "CCMN031.20";
    private static final String winterSemesterCode = "CCMN031.21";

    public UosApiImpl(UosApiParser parser) {
        this.parser = parser;
    }


    private static String wiseRequest(String path, String body, UosSession session)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://wise.uos.ac.kr" + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Cookie", "UOSSESSION=" + session.getWiseSession())
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(request, BodyHandlers.ofString()).body();
    }

    @Override
    public UserBasicInfo getUserBasicInfo(UosSession session) {
        String path = "/Main/onLoad.do";
        String body = "default.locale=CCMN101.KOR";

        try {
            String response = wiseRequest(path, body, session);
            return parser.parseUserBasicInfo(response);
        } catch (IOException | InterruptedException | JSONException e) {
            throw new RuntimeException("Failed to get user basic info", e);
        }
    }
}
