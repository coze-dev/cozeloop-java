package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * ModelInput is the input for model span, for tag key: input
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelInput {
    @JsonProperty("messages")
    private List<ModelMessage> messages;

    @JsonProperty("tools")
    private List<ModelTool> tools;

    @JsonProperty("tool_choice")
    private ModelToolChoice modelToolChoice;

    @JsonProperty("previous_response_id")
    private String previousResponseID;

    // Getters and Setters
    public List<ModelMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ModelMessage> messages) {
        this.messages = messages;
    }

    public List<ModelTool> getTools() {
        return tools;
    }

    public void setTools(List<ModelTool> tools) {
        this.tools = tools;
    }

    public ModelToolChoice getModelToolChoice() {
        return modelToolChoice;
    }

    public void setModelToolChoice(ModelToolChoice modelToolChoice) {
        this.modelToolChoice = modelToolChoice;
    }

    public String getPreviousResponseID() {
        return previousResponseID;
    }

    public void setPreviousResponseID(String previousResponseID) {
        this.previousResponseID = previousResponseID;
    }
}
