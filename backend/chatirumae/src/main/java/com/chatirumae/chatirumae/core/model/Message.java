package com.chatirumae.chatirumae.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    
    @Field("content")
    private String content;
    
    @Field("sender")
    private String sender;
    
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("current_chat_id")
    private String currentChatId;

    // 기본 생성자
    public Message() {}

    // 전체 생성자
    public Message(String id, String content, String sender, LocalDateTime timestamp, 
                   String currentChatId) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
        this.currentChatId = currentChatId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrentChatId() {
        return currentChatId;
    }

    public void setCurrentChatId(String currentChatId) {
        this.currentChatId = currentChatId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                ", currentChatId='" + currentChatId + '\'' +
                '}';
    }
}
