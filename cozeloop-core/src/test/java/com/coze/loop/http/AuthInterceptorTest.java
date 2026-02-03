package com.coze.loop.http;

import com.coze.loop.auth.Auth;
import com.coze.loop.internal.UserAgentUtils;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
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
    void testInterceptAddsHeaders() throws IOException, InterruptedException {
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
        
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer test-token-123");
        assertThat(recordedRequest.getHeader("User-Agent")).isEqualTo(UserAgentUtils.getUserAgent());
        assertThat(recordedRequest.getHeader("X-Coze-Client-User-Agent")).isEqualTo(UserAgentUtils.getClientUserAgent());
    }
}

