package com.coze.loop.prompt;

/** Parameters for getting a prompt. */
public class GetPromptParam {
  private String promptKey;
  private String version;
  private String label;

  public GetPromptParam() {}

  public String getPromptKey() {
    return promptKey;
  }

  public void setPromptKey(String promptKey) {
    this.promptKey = promptKey;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final GetPromptParam param = new GetPromptParam();

    public Builder promptKey(String promptKey) {
      param.promptKey = promptKey;
      return this;
    }

    public Builder version(String version) {
      param.version = version;
      return this;
    }

    public Builder label(String label) {
      param.label = label;
      return this;
    }

    public GetPromptParam build() {
      return param;
    }
  }
}
