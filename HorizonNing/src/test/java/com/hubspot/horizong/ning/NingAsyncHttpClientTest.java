package com.hubspot.horizong.ning;

import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.TestServer;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NingAsyncHttpClientTest {
  private static final TestServer testServer = new TestServer();

  @BeforeClass
  public static void start() throws Exception {
    testServer.start();
  }

  @AfterClass
  public static void stop() throws Exception {
    testServer.stop();
  }

  @Test
  public void testHttp() throws Exception {
    AsyncHttpClient httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest.newBuilder().setUrl("http://localhost:" + testServer.getHttpPort()).build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getAsString()).isEqualTo("<h1>Hello World</h1>");
  }

  @Test
  public void testHttps() throws Exception {
    AsyncHttpClient httpClient = new NingAsyncHttpClient(HttpConfig.newBuilder().setAcceptAllSSL(true).build());

    HttpRequest request = HttpRequest.newBuilder().setUrl("https://localhost:" + testServer.getHttpsPort()).build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getAsString()).isEqualTo("<h1>Hello World</h1>");
  }
}
