package com.chatirumae.chatirumae.infra;

import com.chatirumae.chatirumae.core.interfaces.GptApi;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ChatGptApi implements GptApi {
    
    private final WebClient openAiWebClient;
    
    public ChatGptApi(WebClient openAiWebClient) {
        this.openAiWebClient = openAiWebClient;
    }
    
    @Override
    public Mono<String> generateResponse(String prompt, List<List<String>> responses) {
        GptRequestDto requestBody = new GptRequestDto("gpt-3.5-turbo", prompt);
        
        return openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GptResponseDto.class)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        return response.getChoices().get(0).getMessage().getContent();
                    }
                    return "죄송합니다. 응답을 생성할 수 없습니다.";
                })
                .onErrorReturn("OpenAI API 호출 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}
