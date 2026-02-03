package com.coze.loop.spec.tracespec;

/** SpanValues contains constant values for span tags. */
public final class SpanValues {
  private SpanValues() {}

  // SpanType tag builtin values
  public static final String V_PROMPT_HUB_SPAN_TYPE = "prompt_hub";
  public static final String V_PROMPT_TEMPLATE_SPAN_TYPE = "prompt";
  public static final String V_PROMPT_EXECUTE_SPAN_TYPE = "prompt_execute";
  public static final String V_PROMPT_EXECUTE_STREAMING_SPAN_TYPE = "prompt_execute_streaming";
  public static final String V_MODEL_SPAN_TYPE = "model";
  public static final String V_RETRIEVER_SPAN_TYPE = "retriever";
  public static final String V_TOOL_SPAN_TYPE = "tool";

  public static final int V_ERR_DEFAULT = -1;

  // Tag values for model messages
  public static final String V_ROLE_USER = "user";
  public static final String V_ROLE_SYSTEM = "system";
  public static final String V_ROLE_ASSISTANT = "assistant";
  public static final String V_ROLE_TOOL = "tool";

  public static final String V_TOOL_CHOICE_NONE = "none";
  public static final String V_TOOL_CHOICE_AUTO = "auto";
  public static final String V_TOOL_CHOICE_REQUIRED = "required";
  public static final String V_TOOL_CHOICE_FUNCTION = "function";

  // Tag values for runtime tags
  public static final String V_LANG_GO = "go";
  public static final String V_LANG_JAVA = "java"; // Added java
  public static final String V_LANG_PYTHON = "python";
  public static final String V_LANG_TYPE_SCRIPT = "ts";

  public static final String V_LIB_EINO = "eino";
  public static final String V_LIB_LANG_CHAIN = "langchain";
  public static final String V_LIB_OPENTELEMETRY = "opentelemetry";

  public static final String V_SCENE_CUSTOM = "custom";
  public static final String V_SCENE_PROMPT_HUB = "prompt_hub";
  public static final String V_SCENE_PROMPT_TEMPLATE = "prompt_template";
  public static final String V_SCENE_PROMPT_EXECUTE = "prompt_execute";
  public static final String V_SCENE_PROMPT_EXECUTE_STREAMING = "prompt_execute_streaming";
  public static final String V_SCENE_INTEGRATION = "integration";

  // Tag values for prompt input
  public static final String V_PROMPT_ARG_SOURCE_INPUT = "input";
  public static final String V_PROMPT_ARG_SOURCE_PARTIAL = "partial";
}
