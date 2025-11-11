package com.chatirumae.chatirumae.infra;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EmbeddingRequestDto {
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("input")
    private List<String> input;

    public EmbeddingRequestDto() {}

    public EmbeddingRequestDto(String model, String text) {
        this.model = model;
        this.input = List.of(text);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<String> getInput() {
        return input;
    }

    public void setInput(List<String> input) {
        this.input = input;
    }
}

