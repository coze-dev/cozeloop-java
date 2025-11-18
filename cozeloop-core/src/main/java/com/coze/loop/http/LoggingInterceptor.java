// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.http;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Interceptor to log HTTP requests and responses.
 */
public class LoggingInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        long startTime = System.currentTimeMillis();
        logger.debug("Sending request: {} {}", request.method(), request.url());
        
        // Log request headers
        if (logger.isDebugEnabled()) {
            logger.debug("Request headers:");
            for (String headerName : request.headers().names()) {
                String headerValue = request.header(headerName);
                // Mask sensitive headers
                if ("Authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                    if (headerValue.length() > 20) {
                        headerValue = headerValue.substring(0, 20) + "...";
                    }
                }
                logger.debug("  {}: {}", headerName, headerValue);
            }
        }
        
        Response response = chain.proceed(request);
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Log response (without consuming body)
        ResponseBody body = response.body();
        String bodyString = body != null ? body.string() : "";
        
        logger.debug("Received response: {} {} in {}ms, status: {}, body length: {}",
            request.method(), request.url(), duration, response.code(), bodyString.length());
        
        // Log response headers
        if (logger.isDebugEnabled()) {
            logger.debug("Response headers:");
            for (String headerName : response.headers().names()) {
                String headerValue = response.header(headerName);
                logger.debug("  {}: {}", headerName, headerValue);
            }
        }
        
        // Recreate response with the body we just read
        Response newResponse = response.newBuilder()
            .body(ResponseBody.create(bodyString, body != null ? body.contentType() : null))
            .build();
        
        return newResponse;
    }
}

