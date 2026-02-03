package com.coze.loop.spec.tracespec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RetrieverDocument {
  @JsonProperty("id")
  private String id;

  @JsonProperty("index")
  private String index;

  @JsonProperty("content")
  private String content;

  @JsonProperty("vector")
  private List<Double> vector;

  @JsonProperty("score")
  private Double score;

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public List<Double> getVector() {
    return vector;
  }

  public void setVector(List<Double> vector) {
    this.vector = vector;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }
}
