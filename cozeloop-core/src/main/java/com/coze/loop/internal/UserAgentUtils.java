package com.coze.loop.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for constructing User-Agent headers.
 */
public final class UserAgentUtils {
    private static final String SDK_NAME = "cozeloop-java";
    private static final String SDK_VERSION = Constants.Version;
    private static final String LANG = "java";
    private static final String LANG_VERSION = System.getProperty("java.version", "unknown");
    private static final String OS_NAME = System.getProperty("os.name", "unknown");
    private static final String OS_VERSION = System.getProperty("os.version", "unknown");
    private static final String SCENE = "cozeloop";
    private static final String SOURCE = "openapi";

    private static final String USER_AGENT = SDK_NAME + "/" + SDK_VERSION + " " +
            LANG + "/" + LANG_VERSION + " " +
            OS_NAME + "/" + OS_VERSION;

    private static final String CLIENT_USER_AGENT;

    static {
        Map<String, String> info = new HashMap<>();
        info.put("version", SDK_VERSION);
        info.put("lang", LANG);
        info.put("lang_version", LANG_VERSION);
        info.put("os_name", OS_NAME);
        info.put("os_version", OS_VERSION);
        info.put("scene", SCENE);
        info.put("source", SOURCE);
        CLIENT_USER_AGENT = JsonUtils.toJson(info);
    }

    private UserAgentUtils() {
        // Utility class
    }

    /**
     * Get the standard User-Agent string.
     *
     * @return User-Agent string
     */
    public static String getUserAgent() {
        return USER_AGENT;
    }

    /**
     * Get the Coze-specific client User-Agent JSON string.
     *
     * @return JSON string for X-Coze-Client-User-Agent
     */
    public static String getClientUserAgent() {
        return CLIENT_USER_AGENT;
    }
}
