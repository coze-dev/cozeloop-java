package com.coze.loop.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.coze.loop.auth.Auth;
import com.coze.loop.exception.CozeLoopException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/** Unit tests for HttpClient. */
class HttpClientTest {

  private MockWebServer mockWebServer;

  @Mock private Auth auth;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    when(auth.getToken()).thenReturn("test-token");
    when(auth.getType()).thenReturn("Bearer ");
  }

  @AfterEach
  void tearDown() throws IOException {
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
  }

  @Test
  void testGetRequest() throws IOException {
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}"));

    HttpClient client = new HttpClient(auth);
    String response = client.get(mockWebServer.url("/test").toString());

    assertThat(response).isNotNull();
    assertThat(response).contains("status");
    assertThat(response).contains("ok");

    client.close();
  }

  @Test
  void testPostRequest() throws IOException {
    mockWebServer.enqueue(
        new MockResponse().setResponseCode(200).setBody("{\"result\":\"success\"}"));

    HttpClient client = new HttpClient(auth);
    Map<String, Object> body = new HashMap<>();
    body.put("key", "value");

    String response = client.post(mockWebServer.url("/test").toString(), body);

    assertThat(response).isNotNull();
    assertThat(response).contains("result");

    client.close();
  }

  @Test
  void testPostRequestWithErrorResponse() throws IOException {
    mockWebServer.enqueue(new MockResponse().setResponseCode(404).setBody("Not Found"));

    HttpClient client = new HttpClient(auth);

    assertThatThrownBy(() -> client.post(mockWebServer.url("/test").toString(), new HashMap<>()))
        .isInstanceOf(CozeLoopException.class);

    client.close();
  }

  @Test
  void testGetRequestWithErrorResponse() throws IOException {
    mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

    HttpClient client = new HttpClient(auth);

    assertThatThrownBy(() -> client.get(mockWebServer.url("/test").toString()))
        .isInstanceOf(CozeLoopException.class);

    client.close();
  }

  @Test
  void testPostMultipartRequest() throws IOException {
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("Uploaded"));

    HttpClient client = new HttpClient(auth);
    okhttp3.MultipartBody formData =
        new okhttp3.MultipartBody.Builder()
            .setType(okhttp3.MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "test.txt",
                okhttp3.RequestBody.create("content", okhttp3.MediaType.parse("text/plain")))
            .build();

    String response = client.postMultipart(mockWebServer.url("/upload").toString(), formData);

    assertThat(response).isEqualTo("Uploaded");

    client.close();
  }

  @Test
  void testClose() throws IOException {
    HttpClient client = new HttpClient(auth);

    // Should not throw
    client.close();
  }

  @Test
  void testHttpClientWithCustomConfig() throws IOException {
    HttpConfig config =
        HttpConfig.builder().connectTimeoutSeconds(10).readTimeoutSeconds(20).build();

    HttpClient client = new HttpClient(auth, config);

    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));

    String response = client.get(mockWebServer.url("/test").toString());
    assertThat(response).isEqualTo("OK");

    client.close();
  }
}
