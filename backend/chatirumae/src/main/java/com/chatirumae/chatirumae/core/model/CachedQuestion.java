package com.chatirumae.chatirumae.core.model;

import java.util.List;

/**
 * Redis에 저장되는 질문-답변 캐시 데이터 모델
 */
public class CachedQuestion {
    private String question;
    private String answer;
    private List<Double> embedding;
    private String userId;
    private long timestamp;

    public CachedQuestion() {
    }

    public CachedQuestion(String question, String answer, List<Double> embedding, String userId) {
        this.question = question;
        this.answer = answer;
        this.embedding = embedding;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

