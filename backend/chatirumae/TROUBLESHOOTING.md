# Chat-Irumae 문제 해결 가이드

## VectorStore 검색 오류 해결

### 발생한 오류

```
VectorStore 검색 중 오류 발생: Error while extracting response for type [org.springframework.ai.openai.api.OpenAiApi$EmbeddingList<org.springframework.ai.openai.api.OpenAiApi$Embedding>] and content type [application/octet-stream]
오류 타입: RestClientException
오류 상세: com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
```

### 원인 분석

1. **OpenAI API 키 문제**: 유효하지 않은 API 키로 인한 빈 응답
2. **ChromaDB 연결 문제**: ChromaDB 서버 연결 실패
3. **VectorStore 초기화 문제**: Spring AI 자동 설정 실패

### 해결 방법

#### 1. OpenAI API 키 설정

```bash
# 환경변수 설정
set OPENAI_API_KEY=sk-your-actual-api-key-here

# 또는 set-env.bat 실행
set-env.bat
```

#### 2. ChromaDB 서버 상태 확인

```bash
# ChromaDB 서버 연결 테스트
curl http://54.180.203.59:8000/api/v1/heartbeat
```

#### 3. 애플리케이션 헬스체크

```bash
# 애플리케이션 실행 후 헬스체크
curl http://localhost:3001/api/health
```

### 개선된 기능

#### 1. 향상된 오류 처리

- VectorStore null 체크
- ChromaDB 연결 상태 확인
- GPT API 호출 실패 시 대체 응답

#### 2. 헬스체크 엔드포인트

- `/api/health`: 시스템 상태 확인
- ChromaDB 연결 상태
- VectorStore 상태

#### 3. 개선된 로깅

- 상세한 오류 메시지
- 각 단계별 상태 로그
- 디버깅을 위한 추가 정보

### 설정 파일 확인사항

#### application.properties

```properties
# OpenAI API 설정
spring.ai.openai.api-key=${OPENAI_API_KEY:sk-your-actual-api-key-here}
spring.ai.openai.base-url=https://api.openai.com/v1

# ChromaDB 설정
spring.ai.vectorstore.chroma.url=http://54.180.203.59:8000
spring.ai.vectorstore.chroma.collection-name=SpringAiCollection
spring.ai.vectorstore.chroma.initialize-schema=true
```

### 실행 순서

1. `set-env.bat` 실행하여 API 키 설정
2. ChromaDB 서버 상태 확인
3. `gradlew bootRun` 실행
4. `http://localhost:3001/api/health`로 상태 확인
5. `http://localhost:3001/api/embedding-info`로 embedding 설정 확인
6. `http://localhost:3001/api/test-embedding?text=안녕하세요`로 embedding 테스트
7. `http://localhost:3001/api/chat`로 채팅 테스트

### Embedding 테스트 엔드포인트

- `/api/embedding-info`: EmbeddingClient 및 VectorStore 정보 확인
- `/api/test-embedding?text=테스트텍스트`: Embedding 기능 테스트

### 문제가 지속되는 경우

1. 로그에서 구체적인 오류 메시지 확인
2. ChromaDB 서버가 실행 중인지 확인
3. OpenAI API 키가 유효한지 확인
4. 네트워크 연결 상태 확인
