package com.coze.loop.spec.tracespec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Runtime {
  @JsonProperty("language")
  private String language;

  @JsonProperty("library")
  private String library;

  @JsonProperty("scene")
  private String scene;

  @JsonProperty("scene_version")
  private String sceneVersion;

  @JsonProperty("library_version")
  private String libraryVersion;

  @JsonProperty("loop_sdk_version")
  private String loopSDKVersion;

  @JsonProperty("extra")
  private Map<String, Object> extra;

  // Getters and Setters
  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getLibrary() {
    return library;
  }

  public void setLibrary(String library) {
    this.library = library;
  }

  public String getScene() {
    return scene;
  }

  public void setScene(String scene) {
    this.scene = scene;
  }

  public String getSceneVersion() {
    return sceneVersion;
  }

  public void setSceneVersion(String sceneVersion) {
    this.sceneVersion = sceneVersion;
  }

  public String getLibraryVersion() {
    return libraryVersion;
  }

  public void setLibraryVersion(String libraryVersion) {
    this.libraryVersion = libraryVersion;
  }

  public String getLoopSDKVersion() {
    return loopSDKVersion;
  }

  public void setLoopSDKVersion(String loopSDKVersion) {
    this.loopSDKVersion = loopSDKVersion;
  }

  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(Map<String, Object> extra) {
    this.extra = extra;
  }
}
