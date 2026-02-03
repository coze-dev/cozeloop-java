package com.coze.loop.http;

import com.coze.loop.auth.Auth;
import com.coze.loop.internal.Constants;
import com.coze.loop.internal.UserAgentUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interceptor to add authentication header to requests.
 */
public class AuthInterceptor implements Interceptor {
    private final Auth auth;
    private final ContextPropagators propagators;
    
    public AuthInterceptor(Auth auth) {
        this(auth, null);
    }

    public AuthInterceptor(Auth auth, ContextPropagators propagators) {
        this.auth = auth;
        this.propagators = propagators;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // Add authorization header
        String token = auth.getToken();
        String authType = auth.getType();
        
        Request.Builder builder = original.newBuilder()
            .header("Authorization", authType + " " + token)
            .header("User-Agent", UserAgentUtils.getUserAgent())
            .header("X-Coze-Client-User-Agent", UserAgentUtils.getClientUserAgent());
        
        // Inject Trace context (Span ID, Trace ID, Baggage) into headers
        if (propagators != null) {
            propagators.getTextMapPropagator().inject(Context.current(), builder, (carrier, key, value) -> {
                if (carrier != null && key != null && value != null) {
                    String realKey = key;
                    if (realKey.equals("traceparent")) {
                        realKey = Constants.TraceContextHeaderParent;
                    } else if (realKey.equals("baggage")) {
                        realKey = Constants.TraceContextHeaderBaggage;
                    }
                    carrier.header(realKey, value);
                }
            });
        }
        
        // Add debug headers if present in environment variables
        String ttEnv = System.getenv("x_tt_env");
        if (ttEnv != null && !ttEnv.isEmpty()) {
            builder.header("x-tt-env", ttEnv);
        }
        
        String usePpe = System.getenv("x_use_ppe");
        if (usePpe != null && !usePpe.isEmpty()) {
            builder.header("x-use-ppe", usePpe);
        }
        
        Request request = builder.build();
        
        return chain.proceed(request);
    }
}

