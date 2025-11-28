package com.chatirumae.chatirumae.core.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class PromptUtil {
    public static String getTavilyQueryPrompt(String userMessage) {
        String date = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return """
사용자 질문: %s
사용자 질문을 Tavily Web Search API에 사용할 쿼리 문장(한국어)으로 바꿔줘
참고: 서울시립대학교 Chatbot LLM 서비스
답변형식:
- '서울시립대학교' 포함
- 필수 키워드 사용
- 간결하게 키워드 나열식으로
날짜: %s
쿼리(최종답변): 
""".formatted(date,userMessage);
    }

    public static String getRagPrompt(String referDocuments, String userMessage) {
        return """
당신은 서울시립대학교 챗봇 어시스턴트입니다.
- 주어진 '참고 정보(컨텍스트)'를 바탕으로 사용자의 '질문'에 대해 답변해야 합니다.
- 답변은 반드시 '참고 정보'에 근거해야 하며, 정보에 없는 내용은 답변하지 마세요.
- '참고 정보'는 한국어이고, '사용자 질문'은 사용자가 쓰는 모국어야. 최종 답변은 사용자가 쓴 언어로 해줘.
- '참고 정보'에서 답변을 찾을 수 없다면, "죄송합니다, 관련 정보를 찾을 수 없습니다."라고 한국어로 꼭 답변하세요.
[참고 정보]
%s
[사용자 질문]
%s
[답변]
""".formatted(referDocuments, userMessage);
    }

    public static String getPredictPrompt(int predictCount, String userMessage, String responseMessage) {
        return """
당신은 서울시립대학교 챗봇 어시스턴트입니다.
이전 사용자의 질문과 답변을 참고해서, 다음에 사용자가 물어볼 질문 %s개를 미리 예측해줘.

이전 사용자의 질문: %s
이전 사용자의 답변: %s
답변형식:
- 한국어로 답변
- metadata 없이 내용만(줄글식, 키워드 나열식) 답변

다음에 사용자가 물어볼 질문: 
""".formatted(predictCount, userMessage, responseMessage);
    }
}