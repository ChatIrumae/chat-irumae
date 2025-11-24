package com.chatirumae.chatirumae.infra;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TavilyRequestDto {
    private String query;
    
    @JsonProperty("search_depth")
    private String searchDepth; // "basic" or "advanced"
    
    @JsonProperty("max_results")
    private Integer maxResults;
    
    // @JsonProperty("exclude_domains")
    // private List<String> excludeDomains;

    // 기본 생성자
    public TavilyRequestDto() {
    }

    // 생성자
    public TavilyRequestDto(String query) {
        this.query = query;
        this.searchDepth = "advanced";
        this.maxResults = 3;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchDepth() {
        return searchDepth;
    }

    public void setSearchDepth(String searchDepth) {
        this.searchDepth = searchDepth;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    // public List<String> getExcludeDomains() {
    //     return excludeDomains;
    // }

    // public void setExcludeDomains(List<String> excludeDomains) {
    //     this.excludeDomains = excludeDomains;
    // }
}