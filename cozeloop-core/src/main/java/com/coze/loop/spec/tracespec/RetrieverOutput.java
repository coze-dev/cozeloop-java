package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RetrieverOutput {
    @JsonProperty("documents")
    private List<RetrieverDocument> documents;

    // Getters and Setters
    public List<RetrieverDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<RetrieverDocument> documents) {
        this.documents = documents;
    }
}
