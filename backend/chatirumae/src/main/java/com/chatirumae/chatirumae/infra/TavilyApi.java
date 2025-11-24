package com.chatirumae.chatirumae.infra;

import com.chatirumae.chatirumae.config.TavilyConfig;
import com.chatirumae.chatirumae.infra.TavilyRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Tavily 검색 API를 사용하는 서비스
 * 웹 검색을 통해 최신 정보를 가져옵니다.
 */
@Component
public class TavilyApi {
    
    private final WebClient tavilyWebClient;
    
    public TavilyApi(WebClient tavilyWebClient) {
        this.tavilyWebClient = tavilyWebClient;
    }
    
    /**
     * Tavily API를 사용하여 검색 수행
     */
    public Mono<String> search(String query) {
        TavilyRequestDto request = new TavilyRequestDto(query);
        
        return tavilyWebClient.post()
                .uri("/search")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                    System.err.println("Tavily 검색 중 오류 발생: " + error.getMessage());
                    error.printStackTrace();
                });
    }
}

