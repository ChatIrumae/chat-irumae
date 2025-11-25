package com.chatirumae.chatirumae.core.service;

import com.chatirumae.chatirumae.core.interfaces.GptApi;
import com.chatirumae.chatirumae.core.model.ChatHistory;
import com.chatirumae.chatirumae.core.model.ChatHistorySummary;
import com.chatirumae.chatirumae.infra.ChatGptApi;
import com.chatirumae.chatirumae.infra.TavilyApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ChatService {
    private final VectorStore vectorStore;
    private final GptApi gptApi;
    private final TavilyApi tavilyApi;
    private final ChatHistoryService chatHistoryService;
    private final RedisQuestionCacheService redisCacheService;

    @Value("${chromadb.collection.name}")
    private String collectionName;
    
    @Value("${chat.vectorstore.top-k:5}")
    private int vectorStoreTopK;
    
    @Value("${chat.vectorstore.similarity-threshold:0.7}")
    private double vectorStoreSimilarityThreshold;

    public ChatService(VectorStore vectorStore, ChatGptApi gptApi, 
                      TavilyApi tavilyApi,
                      ChatHistoryService chatHistoryService,
                      RedisQuestionCacheService redisCacheService) {
        this.vectorStore = vectorStore;
        this.gptApi = gptApi;
        this.tavilyApi = tavilyApi;
        this.chatHistoryService = chatHistoryService;
        this.redisCacheService = redisCacheService;
    }

    public List<ChatHistorySummary> getHistory(String userId) {
        try {
            System.out.println("사용자 히스토리 조회: " + userId);
            List<ChatHistory> chatHistories = chatHistoryService.getChatHistoriesByUserId(userId);
            
            // ChatHistory를 ChatHistorySummary로 변환
            return chatHistories.stream()
                .map(chat -> new ChatHistorySummary(chat.getId(), chat.getTitle()))
                .toList();
        } catch (Exception e) {
            System.err.println("히스토리 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // 빈 리스트 반환
        }
    }

    public ChatHistory getChatHistory(String chatId, String userId) {
        try {
            System.out.println("채팅 히스토리 상세 조회: " + chatId + ", 사용자: " + userId);
            return chatHistoryService.getChatHistoryById(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("채팅 히스토리를 찾을 수 없습니다: " + chatId));
        } catch (Exception e) {
            System.err.println("채팅 히스토리 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String getResponse(String userMessage, Date date, String currentChatId, String sender) {
        try {
            System.out.println("사용자 메시지: " + userMessage + date + currentChatId + sender);

            // 사용자 메시지를 ChatHistory에 추가 (없으면 새로 생성)
            // sender를 userId로 사용
            chatHistoryService.addMessageToChatHistory(currentChatId, sender, userMessage, sender);

            // Tavily Query 생성
            String searchQuery = null;
            try {
                final String TAVILY_QUERY_PROMPT_TEMPLATE = "사용자 질문을 Tavily Web Search API에 사용할 쿼리 문장(한국어)으로 바꿔줘\n문맥: 서울시립대학교 LLM 챗봇 사용자를 대상으로 한 서비스\n답변형식:\n- '서울시립대학교' 포함\n- 필수 키워드 사용\n- 간결하게\n사용자 질문: %s\n쿼리: \n";
                String searchPrompt = String.format(TAVILY_QUERY_PROMPT_TEMPLATE, userMessage);
                System.out.println("Search Prompt: " + searchPrompt);
                searchQuery = gptApi.generateQuery(searchPrompt).block();
                System.out.println("Search Query: " + searchQuery);
            } catch (Exception gptError) {
                System.err.println("GPT API 호출 중 오류 발생: " + gptError.getMessage());
                gptError.printStackTrace();
            }

            // Redis 캐시에서 유사한 질문 검색
            try {
                String cachedAnswer = redisCacheService.findSimilarQuestion(userMessage, sender);
                if (cachedAnswer != null) {
                    System.out.println("캐시에서 답변을 찾았습니다!");
                    // 캐시된 답변을 ChatHistory에 추가
                    chatHistoryService.addMessageToChatHistory(currentChatId, sender, cachedAnswer, "assistant");
                    return cachedAnswer;
                }
                System.out.println("캐시에서 유사한 질문을 찾지 못했습니다.");
            } catch (Exception cacheError) {
                System.err.println("Redis 캐시 검색 중 오류 발생: " + cacheError.getMessage());
                cacheError.printStackTrace();
                System.out.println("캐시 오류를 무시하고 계속 진행합니다.");
            }

            // Web Search
            String referDocuments = null;
            try {
                referDocuments = tavilyApi.search(searchQuery).block();
            } catch (Exception tavilyError) {
                System.err.println("Tavily API 호출 중 오류 발생: " + tavilyError.getMessage());
                tavilyError.printStackTrace();
            }

            System.out.println("");
            System.out.println("RESPONSE");
            // GPT Response
            String response = null;
            try {
                 final String RAG_PROMPT_TEMPLATE = """
                    당신은 서울시립대학교 LLM 챗봇 어시스턴트입니다.
                    주어진 '참고 정보(컨텍스트)'를 바탕으로 사용자의 '질문'에 대해 답변해야 합니다.
                    답변은 반드시 '참고 정보'에 근거해야 하며, 정보에 없는 내용은 답변하지 마세요.
                    '참고 정보'에서 답변을 찾을 수 없다면, "죄송합니다, 관련 정보를 찾을 수 없습니다."라고 답변하세요.
                    사용자가 어색하지 않게 친근한 말투로 정보를 잘 정리해서 답변하세요.                
                    [참고 정보]
                    %s
                
                    [사용자 질문]
                    %s
                
                    [답변]
                    """;
                    response = gptApi.generateQuery(String.format(RAG_PROMPT_TEMPLATE, referDocuments, userMessage)).block();
                    System.out.println("Response: " + response);
            } catch (Exception gptError) {
                System.err.println("GPT API 호출 중 오류 발생: " + gptError.getMessage());
                gptError.printStackTrace();
            }

            // VectorStore 검색 시도
            // try {
            //     System.out.println("검색 설정: TopK=" + vectorStoreTopK + ", 유사도 임계값=" + vectorStoreSimilarityThreshold);

            //     // SearchRequest를 사용하여 명시적으로 검색 조건 설정
            //     SearchRequest request = SearchRequest.query(userMessage)
            //             .withTopK(vectorStoreTopK) // 최대 반환할 문서 수
            //             .withSimilarityThreshold(vectorStoreSimilarityThreshold); // 유사도 임계값 (이 값 이상인 문서만 반환)

            //     System.out.println("Embedding을 통한 유사도 검색을 시작합니다...");
            //     List<Document> similarDocuments = vectorStore.similaritySearch(request);
                
            //     if (similarDocuments != null && !similarDocuments.isEmpty()) {
            //         StringBuilder contextBuilder = new StringBuilder();

            //         for (int i = 0; i < similarDocuments.size(); i++) {
            //             Document document = similarDocuments.get(i);
            //             String content = document.getContent(); // 문서 내용 가져오기
            //             String metadata = document.getMetadata().toString();

            //             System.out.println(document.getContent());

            //             contextBuilder.append("--- 참고 문서 " + (i + 1) + " ---\n");
            //             contextBuilder.append(content);
            //             contextBuilder.append("\n\n"); // 문서 사이에 공백 추가

            //             System.out.println("-------------------------");
            //         }

            //         final String RAG_PROMPT_TEMPLATE = """
            //         당신은 서울시립대학교 LLM 챗봇 어시스턴트입니다.
            //         주어진 '참고 정보(컨텍스트)'를 바탕으로 사용자의 '질문'에 대해 답변해야 합니다.
            //         답변은 반드시 '참고 정보'에 근거해야 하며, 정보에 없는 내용은 답변하지 마세요.
            //         '참고 정보'에서 답변을 찾을 수 없다면, "죄송합니다, 관련 정보를 찾을 수 없습니다."라고 답변하세요.
            //         사용자가 어색하지 않게 친근한 말투로 정보를 잘 정리해서 답변하세요.                
            //         [참고 정보]
            //         %s
                
            //         [사용자 질문]
            //         %s
                
            //         [답변]
            //         """;

            //         } catch (Exception cacheError) {
            //             System.err.println("Redis 캐시 저장 중 오류 발생: " + cacheError.getMessage());
            //             // 캐시 저장 실패는 무시하고 계속 진행
            //         }
                    
            //         return response;
            //     } else {
            //         System.out.println("유사한 문서를 찾지 못했습니다. 컨텍스트 없이 GPT API를 호출합니다.");
            //     }
            // } catch (Exception vectorStoreError) {
            //     System.err.println("VectorStore 검색 중 오류 발생: " + vectorStoreError.getMessage());
            //     System.err.println("오류 타입: " + vectorStoreError.getClass().getSimpleName());
            //     System.err.println("오류 상세: " + vectorStoreError.getCause());
            //     vectorStoreError.printStackTrace();
            //     System.out.println("VectorStore 오류를 무시하고 직접 GPT API를 호출합니다.");
            // }
            
            // VectorStore 검색 실패 시 또는 결과가 없을 때 컨텍스트 없이 GPT API 호출
            // try {
            //     String response = "답변을 찾지 못했습니다.";
                
            //     // AI 응답 메시지를 ChatHistory에 추가
            //     chatHistoryService.addMessageToChatHistory(currentChatId, sender, response, "assistant");

                
            //     return response;
            // } catch (Exception gptError) {
            //     System.err.println("GPT API 호출 중 오류 발생: " + gptError.getMessage());
            //     gptError.printStackTrace();
            //     String errorResponse = "죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
                
            //     // 에러 메시지도 ChatHistory에 추가
            //     chatHistoryService.addMessageToChatHistory(currentChatId, sender, errorResponse, "assistant");
                
            //     return errorResponse;
            // }

            return response;
        } catch (Exception e) {
            System.err.println("Error in ChatService.getResponse: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 현재 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    public void predict(String userMessage, String responseMessage, Date timestamp, String currentChatId, String sender) {
        // TODO 일단 1개만 예측
        // TODO FAILED FILTERING
        final String PREDICT_PROMPT_TEMPLATE = """
                    당신은 서울시립대학교 LLM 챗봇 어시스턴트입니다.
                    이전 사용자의 질문과 답변을 참고해서, 다음에 사용자가 물어볼 질문 %s개를 미리 예측해줘.
                    이전 사용자의 질문: %s
                    이전 사용자의 답변: %s
                    답변형식:
                    - 한국어로 답변
                    - 부가 정보를 제외하고, 질문의 내용만 답변
                    """;
        String prompt = String.format(PREDICT_PROMPT_TEMPLATE, 1, userMessage, responseMessage);
        String predictedQuestion = gptApi.generatePrediction(prompt).block();
        try{
            String answer = getResponse(predictedQuestion, timestamp, currentChatId, sender);
            if (answer == null || answer.isEmpty() || answer.equals("FAILED")) {
                throw new Exception("Answer is null or empty or failed");
            }
            System.out.println("");
            System.out.println("PREDICT");
            System.out.println("Predicted Question: " + predictedQuestion);
            System.out.println("Answer: " + answer);
            redisCacheService.cacheQuestionAnswer(predictedQuestion, answer, sender);
        } catch (Exception e) {
            System.err.println("Error in ChatService.predict: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
