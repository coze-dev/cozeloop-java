package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RetrieverCallOption {
    @JsonProperty("top_k")
    private Long topK;

    @JsonProperty("min_score")
    private Double minScore;

    @JsonProperty("filter")
    private String filter;

    // Getters and Setters
    public Long getTopK() {
        return topK;
    }

    public void setTopK(Long topK) {
        this.topK = topK;
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
