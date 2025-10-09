package com.chatirumae.chatirumae.infra;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GptResponseDto {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    public GptResponseDto() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public static class Choice {
        private int index;
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;

        public Choice() {}

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        @JsonProperty("finish_reason")
        public String getFinishReason() {
            return finishReason;
        }

        @JsonProperty("finish_reason")
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }

    public static class Message {
        private String role;
        private String content;

        public Message() {}

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

    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;

        public Usage() {}

        @JsonProperty("prompt_tokens")
        public int getPromptTokens() {
            return promptTokens;
        }

        @JsonProperty("prompt_tokens")
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        @JsonProperty("completion_tokens")
        public int getCompletionTokens() {
            return completionTokens;
        }

        @JsonProperty("completion_tokens")
        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        @JsonProperty("total_tokens")
        public int getTotalTokens() {
            return totalTokens;
        }

        @JsonProperty("total_tokens")
        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}
