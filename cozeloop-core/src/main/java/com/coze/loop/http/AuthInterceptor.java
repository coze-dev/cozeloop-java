package com.coze.loop.http;

import com.coze.loop.auth.Auth;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interceptor to add authentication header to requests.
 */
public class AuthInterceptor implements Interceptor {
    private final Auth auth;
    
    public AuthInterceptor(Auth auth) {
        this.auth = auth;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // Add authorization header
        String token = auth.getToken();
        String authType = auth.getType();
        
        Request request = original.newBuilder()
            .header("Authorization", authType + " " + token)
            .header("User-Agent", "CozeLoop-Java-SDK/1.0.0")
            .build();
        
        return chain.proceed(request);
    }
}

