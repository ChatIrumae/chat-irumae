package com.chatirumae.chatirumae.infra;

import com.chatirumae.chatirumae.core.interfaces.VectorDb;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ChromaVectorDb implements VectorDb {

    private final WebClient webClient;

    public ChromaVectorDb(WebClient.Builder webClientBuilder, @Value("${chromadb.api.url}") String chromaApiUrl) {
        this.webClient = webClientBuilder.baseUrl(chromaApiUrl).build();
    }

    /**
     * 문서를 컬렉션에 추가합니다.
     * ChromaDB가 내부적으로 Embedding을 생성합니다.
     */
    public void addDocuments(String collectionName, List<String> ids, List<String> documents, List<Map<String, String>> metadatas) {
        // 컬렉션 ID를 가져오는 API 호출 (없으면 생성)
        String collectionId = getOrCreateCollection(collectionName).block();

        ChromaDtos.AddRequest requestBody = new ChromaDtos.AddRequest(ids, documents, metadatas);

        webClient.post()
                .uri("/collections/{collectionId}/add", collectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class) // 응답 본문이 필요 없을 때
                .block(); // 동기적으로 결과를 기다림
    }

    /**
     * 텍스트로 유사도 높은 문서를 검색합니다.
     */
    public ChromaDtos.QueryResult query(String collectionName, String queryText, int nResults) {
        String collectionId = getOrCreateCollection(collectionName).block();

        ChromaDtos.QueryRequest requestBody = new ChromaDtos.QueryRequest(List.of(queryText), nResults);

        return webClient.post()
                .uri("/collections/{collectionId}/query", collectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ChromaDtos.QueryResult.class)
                .block();
    }

    /**
     * 컬렉션을 조회하거나 생성합니다.
     */
    private Mono<String> getOrCreateCollection(String collectionName) {
        Map<String, String> request = Map.of("name", collectionName);
        return webClient.post()
                .uri("/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("id"));
    }
}


