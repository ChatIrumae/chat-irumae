# Chat History API 문서

## 개요

MongoDB를 사용한 채팅 히스토리 관리 API입니다.

## 엔드포인트

### 1. 사용자의 모든 채팅 히스토리 조회

```
GET /api/chat-history/user/{userId}
```

**응답:**

```json
[
  {
    "id": "chat_1703123456789_abc123def",
    "title": "안녕하세요",
    "messages": [...],
    "createdAt": "2023-12-21T10:30:00",
    "updatedAt": "2023-12-21T10:35:00",
    "userId": "user123"
  }
]
```

### 2. 특정 채팅 히스토리 조회

```
GET /api/chat-history/{chatId}/user/{userId}
```

### 3. 새 채팅 히스토리 생성

```
POST /api/chat-history/user/{userId}
```

**요청 본문:**

```json
{
  "title": "새 채팅"
}
```

### 4. 채팅 히스토리 업데이트 (메시지 추가)

```
PUT /api/chat-history/{chatId}/user/{userId}
```

**요청 본문:**

```json
{
  "messages": [
    {
      "id": "msg123",
      "content": "안녕하세요",
      "sender": "user",
      "timestamp": "2023-12-21T10:30:00",
      "currentChatId": "chat_1703123456789_abc123def"
    }
  ]
}
```

### 5. 메시지 저장

```
POST /api/chat-history/message
```

**요청 본문:**

```json
{
  "content": "안녕하세요",
  "sender": "user",
  "currentChatId": "chat_1703123456789_abc123def"
}
```

### 6. 채팅 히스토리 삭제

```
DELETE /api/chat-history/{chatId}/user/{userId}
```

### 7. 채팅 히스토리 제목 업데이트

```
PATCH /api/chat-history/{chatId}/user/{userId}/title
```

**요청 본문:**

```json
{
  "title": "새로운 제목"
}
```

### 8. 채팅 히스토리 검색

```
GET /api/chat-history/user/{userId}/search?q=검색어
```

### 9. 사용자 채팅 히스토리 요약 조회

```
GET /api/history/{userId}
```

**응답:**

```json
[
  {
    "_id": "chat_1703123456789_abc123def",
    "title": "안녕하세요"
  },
  {
    "_id": "chat_1703123456790_def456ghi",
    "title": "질문이 있습니다"
  }
]
```

### 10. 특정 채팅 히스토리 상세 조회

```
GET /api/history/{userId}/{chatId}
```

**응답:**

```json
{
  "id": "chat_1703123456789_abc123def",
  "title": "안녕하세요",
  "messages": [
    {
      "id": "msg123",
      "content": "안녕하세요",
      "sender": "user123",
      "timestamp": "2023-12-21T10:30:00",
      "currentChatId": "chat_1703123456789_abc123def"
    },
    {
      "id": "msg124",
      "content": "안녕하세요! 무엇을 도와드릴까요?",
      "sender": "assistant",
      "timestamp": "2023-12-21T10:30:05",
      "currentChatId": "chat_1703123456789_abc123def"
    }
  ],
  "createdAt": "2023-12-21T10:30:00",
  "updatedAt": "2023-12-21T10:35:00",
  "userId": "user123"
}
```

## 데이터 모델

### ChatHistory

```java
{
  "id": "String",           // 채팅 히스토리 ID (프론트엔드에서 생성)
  "title": "String",        // 채팅 제목
  "messages": "Message[]",  // 메시지 리스트
  "createdAt": "LocalDateTime", // 생성 시간
  "updatedAt": "LocalDateTime", // 수정 시간
  "userId": "String"        // 사용자 ID
}
```

### Message

```java
{
  "id": "String",           // 메시지 ID
  "content": "String",      // 메시지 내용
  "sender": "String",       // 발신자 (user/assistant)
  "timestamp": "LocalDateTime", // 시간
  "currentChatId": "String" // 채팅 ID
}
```

### ChatHistorySummary

```java
{
  "_id": "String",          // 채팅 히스토리 ID
  "title": "String"         // 채팅 제목
}
```

## 새로운 채팅 플로우

### 1. 프론트엔드에서 chatId 생성

```javascript
const chatId =
  "chat_" + Date.now() + "_" + Math.random().toString(36).substr(2, 9);
```

### 2. 메시지 전송 (자동으로 ChatHistory 생성/업데이트)

```
POST /api/chat
```

**요청 본문:**

```json
{
  "content": "안녕하세요",
  "sender": "user123",
  "currentChatId": "chat_1703123456789_abc123def",
  "timestamp": "2023-12-21T10:30:00"
}
```

**동작:**

- `currentChatId`로 기존 ChatHistory 검색
- 존재하면 메시지 추가 (sender를 userId로 사용하여 권한 확인)
- 존재하지 않으면 새 ChatHistory 생성 (sender를 userId로 사용, 첫 메시지로 제목 자동 생성)

## 사용 예시

### 1. 새 채팅 시작

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "content": "안녕하세요",
    "sender": "user123",
    "currentChatId": "chat_1703123456789_abc123def",
    "timestamp": "2023-12-21T10:30:00"
  }'
```

### 2. 채팅 히스토리 조회

```bash
curl -X GET http://localhost:8080/api/chat-history/user/user123
```

### 3. 특정 채팅 조회

```bash
curl -X GET http://localhost:8080/api/chat-history/chat_1703123456789_abc123def/user/user123
```
