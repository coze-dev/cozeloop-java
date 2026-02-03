package com.coze.loop.spec.tracespec;

/**
 * SpanKeys contains constant keys for span tags.
 */
public final class SpanKeys {
    private SpanKeys() {}

    // Tags for model-type span
    public static final String CALL_OPTIONS = "call_options";
    public static final String STREAM = "stream";
    public static final String REASONING_TOKENS = "reasoning_tokens";
    public static final String REASONING_DURATION = "reasoning_duration";

    // Tags for tool-type span
    public static final String TOOL_CALL_ID = "tool_call_id";

    // Tags for retriever-type span
    public static final String RETRIEVER_PROVIDER = "retriever_provider";
    public static final String VIKING_DB_NAME = "vikingdb_name";
    public static final String VIKING_DB_REGION = "vikingdb_region";
    public static final String ES_NAME = "es_name";
    public static final String ES_INDEX = "es_index";
    public static final String ES_CLUSTER = "es_cluster";

    // Tags for prompt-type span
    public static final String PROMPT_PROVIDER = "prompt_provider";
    public static final String PROMPT_KEY = "prompt_key";
    public static final String PROMPT_VERSION = "prompt_version";
    public static final String PROMPT_LABEL = "prompt_label";

    // Internal experimental fields
    public static final String SPAN_TYPE = "span_type";
    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    public static final String ERROR = "error";
    public static final String RUNTIME = "runtime";

    public static final String MODEL_PROVIDER = "model_provider";
    public static final String MODEL_NAME = "model_name";
    public static final String INPUT_TOKENS = "input_tokens";
    public static final String INPUT_CACHED_TOKENS = "input_cached_tokens";
    public static final String OUTPUT_TOKENS = "output_tokens";
    public static final String TOKENS = "tokens";
    public static final String MODEL_PLATFORM = "model_platform";
    public static final String MODEL_IDENTIFICATION = "model_identification";
    public static final String TOKEN_USAGE_BACKUP = "token_usage_backup";
    public static final String LATENCY_FIRST_RESP = "latency_first_resp";

    public static final String CALL_TYPE = "call_type";
    public static final String LOG_ID = "log_id";
    public static final String TRACE_ID = "trace_id";


    // otel attribute key
    public static final String COZELOOP_SYSTEM_TAG_RUNTIME = "cozeloop.system_tag_runtime";
    public static final String COZELOOP_INPUT = "cozeloop.input";
    public static final String COZELOOP_OUTPUT = "cozeloop.output";
    public static final String COZELOOP_ERROR = "error.type";
    public static final String COZELOOP_STATUS_CODE = "cozeloop.status_code";
    public static final String USER_ID = "user.id";
    public static final String MESSAGING_MESSAGE_ID = "messaging.message.id";
    public static final String SESSION_ID = "session.id";
    public static final String GEN_AI_PROVIDER_NAME = "gen_ai.provider.name";
    public static final String LLM_MODEL_NAME = "llm.model_name";
    public static final String GEN_AI_REQUEST_TEMPERATURE = "gen_ai.request.temperature";
    public static final String GEN_AI_REQUEST_TOP_P = "gen_ai.request.top_p";
    public static final String GEN_AI_REQUEST_TOP_K = "gen_ai.request.top_k";
    public static final String GEN_AI_REQUEST_MAX_TOKENS = "gen_ai.request.max_tokens";
    public static final String GEN_AI_REQUEST_FREQUENCY_PENALTY = "gen_ai.request.frequency_penalty";
    public static final String GEN_AI_REQUEST_PRESENCE_PENALTY = "gen_ai.request.presence_penalty";
    public static final String GEN_AI_REQUEST_STOP_SEQUENCES = "gen_ai.request.stop_sequences";
    public static final String GEN_AI_USAGE_INPUT_TOKENS = "gen_ai.usage.input_tokens";
    public static final String GEN_AI_USAGE_OUTPUT_TOKENS = "gen_ai.usage.output_tokens";
    public static final String COZELOOP_TIME_TO_FIRST_TOKEN = "cozeloop.time_to_first_token";
    public static final String SERVICE_NAME = "service.name";
    public static final String COZELOOP_LOGID = "cozeloop.logid";
    public static final String DEPLOYMENT_ENV = "deployment_env";
    public static final String COZELOOP_SPAN_TYPE = "cozeloop.span_type";
}
