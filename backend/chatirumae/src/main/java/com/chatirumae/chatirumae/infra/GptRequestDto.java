package com.chatirumae.chatirumae.infra;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public class GptRequestDto {
    private String model;
    private List<Message> messages;
    private double temperature;
    private int maxTokens;

    public GptRequestDto() {}

    public GptRequestDto(String model, String prompt) {
        this.model = model;
        this.messages = List.of(new Message("user", prompt));
        this.temperature = 0.7;
        this.maxTokens = 1000;
    }

    // Getters and Setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @JsonProperty("max_tokens")
    public int getMaxTokens() {
        return maxTokens;
    }

    @JsonProperty("max_tokens")
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
