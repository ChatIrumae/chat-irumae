package com.chatirumae.chatirumae.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chat_histories")
public class ChatHistory {
    @Id
    private String id;
    
    @Field("title")
    private String title;
    
    @Field("messages")
    private List<Message> messages;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("user_id")
    private String userId;

    // 기본 생성자
    public ChatHistory() {}

    // 전체 생성자
    public ChatHistory(String id, String title, List<Message> messages, 
                      LocalDateTime createdAt, LocalDateTime updatedAt, String userId) {
        this.id = id;
        this.title = title;
        this.messages = messages;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ChatHistory{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", messages=" + messages +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", userId='" + userId + '\'' +
                '}';
    }
}
