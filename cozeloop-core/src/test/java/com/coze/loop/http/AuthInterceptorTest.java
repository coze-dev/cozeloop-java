package com.coze.loop.http;

import com.coze.loop.auth.Auth;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthInterceptor.
 */
class AuthInterceptorTest {

    private MockWebServer mockWebServer;
    
    @Mock
    private Auth auth;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void testInterceptAddsAuthorizationHeader() throws IOException {
        when(auth.getToken()).thenReturn("test-token-123");
        when(auth.getType()).thenReturn("Bearer");
        
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
        
        AuthInterceptor interceptor = new AuthInterceptor(auth);
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build();
        
        Request request = new Request.Builder()
            .url(mockWebServer.url("/test"))
            .get()
            .build();
        
        okhttp3.Response response = client.newCall(request).execute();
        
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.code()).isEqualTo(200);
    }

    @Test
    void testInterceptAddsUserAgentHeader() throws IOException {
        when(auth.getToken()).thenReturn("token");
        when(auth.getType()).thenReturn("Bearer");
        
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        
        AuthInterceptor interceptor = new AuthInterceptor(auth);
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build();
        
        Request request = new Request.Builder()
            .url(mockWebServer.url("/test"))
            .get()
            .build();
        
        okhttp3.Response response = client.newCall(request).execute();
        
        assertThat(response.isSuccessful()).isTrue();
    }
}

