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
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GptResponseDto.class)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        return response.getChoices().get(0).getMessage().getContent();
                    }
                    return "죄송합니다. 응답을 생성할 수 없습니다.";
                })
                .doOnError(error -> {
                    // !!! 실제 에러가 여기 출력됩니다 !!!
                    System.err.println("!!! OpenAI API 호출 실패 !!!");
                    System.err.println(error.getMessage());
                    // (더 자세한 디버깅을 위해 전체 스택 트레이스를 출력할 수도 있습니다)
                    // error.printStackTrace();
                })
                .onErrorReturn("OpenAI API 호출 중 오류가 발생했습니다. 다시 시도해주세요.");
    }

    @Override
    public Mono<String> generateQuery(String prompt) {
        GptRequestDto requestBody = new GptRequestDto("gpt-3.5-turbo", prompt);

        return openAiWebClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GptResponseDto.class)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        return response.getChoices().get(0).getMessage().getContent();
                    }
                    return "죄송합니다. 응답을 생성할 수 없습니다.";
                })
                .doOnError(error -> {
                    System.err.println("GPT API 호출 중 오류가 발생했습니다. " + error.getMessage());
                    error.printStackTrace();
                })
                .onErrorReturn("GPT API 호출 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}
