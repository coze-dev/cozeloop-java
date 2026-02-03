package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelMessagePart {
  @JsonProperty("type")
  private String type;

  @JsonProperty("text")
  private String text;

  @JsonProperty("image_url")
  private ModelImageURL imageURL;

  @JsonProperty("file_url")
  private ModelFileURL fileURL;

  @JsonProperty("audio_url")
  private ModelAudioURL audioURL;

  @JsonProperty("video_url")
  private ModelVideoURL videoURL;

  @JsonProperty("signature")
  private String signature;

  // Getters and Setters
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public ModelImageURL getImageURL() {
    return imageURL;
  }

  public void setImageURL(ModelImageURL imageURL) {
    this.imageURL = imageURL;
  }

  public ModelFileURL getFileURL() {
    return fileURL;
  }

  public void setFileURL(ModelFileURL fileURL) {
    this.fileURL = fileURL;
  }

  public ModelAudioURL getAudioURL() {
    return audioURL;
  }

  public void setAudioURL(ModelAudioURL audioURL) {
    this.audioURL = audioURL;
  }

  public ModelVideoURL getVideoURL() {
    return videoURL;
  }

  public void setVideoURL(ModelVideoURL videoURL) {
    this.videoURL = videoURL;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }
}
