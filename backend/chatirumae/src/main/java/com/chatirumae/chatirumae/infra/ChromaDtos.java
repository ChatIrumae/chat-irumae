package com.chatirumae.chatirumae.infra;

import java.util.List;

public final class ChromaDtos {
    // 문서 추가 요청 DTO
    public record AddRequest(
            java.util.List<String> ids,
            java.util.List<String> documents,
            java.util.List<java.util.Map<String, String>> metadatas
    ) {}

    // 쿼리(검색) 요청 DTO
    public record QueryRequest(
            java.util.List<String> query_texts,
            int n_results
    ) {}

    // 쿼리 결과 DTO
    public record QueryResult(
            java.util.List<java.util.List<String>> ids,
            java.util.List<java.util.List<Float>> distances,
            java.util.List<java.util.List<String>> documents,
            java.util.List<java.util.List<java.util.Map<String, String>>> metadatas
    ) {
        public List<List<String>> getDocuments() {
            return documents;
        }
    }
}