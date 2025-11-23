package com.chatirumae.chatirumae.infra;

import com.chatirumae.chatirumae.core.interfaces.QueryPreProcessor;

public class BasicQueryPreProcessor implements QueryPreProcessor {

    @Override
    public String preprocess(String rawQuery) {
        if (rawQuery == null) return "";

        // 1. 좌우 공백 제거 (Trim)
        String processed = rawQuery.trim();

        // 2. 불필요한 특수문자 제거 (이모지 등 제거하고 한글, 영어, 숫자, 기본 문장부호만 남김)
        processed = processed.replaceAll("[^a-zA-Z0-9가-힣\\s?.,]", "");

        // 3. 다중 공백을 단일 공백으로 치환
        processed = processed.replaceAll("\\s+", " ");

        return processed;
    }
}
