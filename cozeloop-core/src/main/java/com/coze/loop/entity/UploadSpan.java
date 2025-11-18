package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Span entity for uploading to CozeLoop platform.
 */
public class UploadSpan {
    @JsonProperty("started_at_micros")
    private long startedAtMicros;
    
    @JsonProperty("log_id")
    private String logId;
    
    @JsonProperty("span_id")
    private String spanId;
    
    @JsonProperty("parent_id")
    private String parentId;
    
    @JsonProperty("trace_id")
    private String traceId;
    
    @JsonProperty("duration_micros")
    private long durationMicros;
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("workspace_id")
    private String workspaceId;
    
    @JsonProperty("span_name")
    private String spanName;
    
    @JsonProperty("span_type")
    private String spanType;
    
    @JsonProperty("status_code")
    private int statusCode;
    
    @JsonProperty("input")
    private String input;
    
    @JsonProperty("output")
    private String output;
    
    @JsonProperty("object_storage")
    private String objectStorage;
    
    @JsonProperty("system_tags_string")
    private Map<String, String> systemTagsString;
    
    @JsonProperty("system_tags_long")
    private Map<String, Long> systemTagsLong;
    
    @JsonProperty("system_tags_double")
    private Map<String, Double> systemTagsDouble;
    
    @JsonProperty("tags_string")
    private Map<String, String> tagsString;
    
    @JsonProperty("tags_long")
    private Map<String, Long> tagsLong;
    
    @JsonProperty("tags_double")
    private Map<String, Double> tagsDouble;
    
    @JsonProperty("tags_bool")
    private Map<String, Boolean> tagsBool;
    
    public UploadSpan() {
    }
    
    // Getters and Setters
    public long getStartedAtMicros() {
        return startedAtMicros;
    }
    
    public void setStartedAtMicros(long startedAtMicros) {
        this.startedAtMicros = startedAtMicros;
    }
    
    public String getLogId() {
        return logId;
    }
    
    public void setLogId(String logId) {
        this.logId = logId;
    }
    
    public String getSpanId() {
        return spanId;
    }
    
    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getTraceId() {
        return traceId;
    }
    
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
    
    public long getDurationMicros() {
        return durationMicros;
    }
    
    public void setDurationMicros(long durationMicros) {
        this.durationMicros = durationMicros;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    public String getSpanName() {
        return spanName;
    }
    
    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }
    
    public String getSpanType() {
        return spanType;
    }
    
    public void setSpanType(String spanType) {
        this.spanType = spanType;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public String getObjectStorage() {
        return objectStorage;
    }
    
    public void setObjectStorage(String objectStorage) {
        this.objectStorage = objectStorage;
    }
    
    public Map<String, String> getSystemTagsString() {
        return systemTagsString;
    }
    
    public void setSystemTagsString(Map<String, String> systemTagsString) {
        this.systemTagsString = systemTagsString;
    }
    
    public Map<String, Long> getSystemTagsLong() {
        return systemTagsLong;
    }
    
    public void setSystemTagsLong(Map<String, Long> systemTagsLong) {
        this.systemTagsLong = systemTagsLong;
    }
    
    public Map<String, Double> getSystemTagsDouble() {
        return systemTagsDouble;
    }
    
    public void setSystemTagsDouble(Map<String, Double> systemTagsDouble) {
        this.systemTagsDouble = systemTagsDouble;
    }
    
    public Map<String, String> getTagsString() {
        return tagsString;
    }
    
    public void setTagsString(Map<String, String> tagsString) {
        this.tagsString = tagsString;
    }
    
    public Map<String, Long> getTagsLong() {
        return tagsLong;
    }
    
    public void setTagsLong(Map<String, Long> tagsLong) {
        this.tagsLong = tagsLong;
    }
    
    public Map<String, Double> getTagsDouble() {
        return tagsDouble;
    }
    
    public void setTagsDouble(Map<String, Double> tagsDouble) {
        this.tagsDouble = tagsDouble;
    }
    
    public Map<String, Boolean> getTagsBool() {
        return tagsBool;
    }
    
    public void setTagsBool(Map<String, Boolean> tagsBool) {
        this.tagsBool = tagsBool;
    }
}

