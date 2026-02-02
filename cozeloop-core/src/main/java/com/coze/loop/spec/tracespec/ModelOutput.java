package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * ModelOutput is the output for model span, for tag key: output
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelOutput {
    @JsonProperty("id")
    private String id;

    @JsonProperty("choices")
    private List<ModelChoice> choices;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ModelChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<ModelChoice> choices) {
        this.choices = choices;
    }
}
