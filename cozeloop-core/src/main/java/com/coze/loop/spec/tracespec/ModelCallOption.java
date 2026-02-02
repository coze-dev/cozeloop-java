package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * ModelCallOption is the option for model span, for tag key: call_options
 */
public class ModelCallOption {
    @JsonProperty("temperature")
    private Float temperature;

    @JsonProperty("max_tokens")
    private Long maxTokens;

    @JsonProperty("stop")
    private List<String> stop;

    @JsonProperty("top_p")
    private Float topP;

    @JsonProperty("n")
    private Long n;

    @JsonProperty("top_k")
    private Long topK;

    @JsonProperty("presence_penalty")
    private Float presencePenalty;

    @JsonProperty("frequency_penalty")
    private Float frequencyPenalty;

    @JsonProperty("reasoning_effort")
    private String reasoningEffort;

    // Getters and Setters
    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Long getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Long maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public Float getTopP() {
        return topP;
    }

    public void setTopP(Float topP) {
        this.topP = topP;
    }

    public Long getN() {
        return n;
    }

    public void setN(Long n) {
        this.n = n;
    }

    public Long getTopK() {
        return topK;
    }

    public void setTopK(Long topK) {
        this.topK = topK;
    }

    public Float getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Float presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public Float getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Float frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public String getReasoningEffort() {
        return reasoningEffort;
    }

    public void setReasoningEffort(String reasoningEffort) {
        this.reasoningEffort = reasoningEffort;
    }
}
