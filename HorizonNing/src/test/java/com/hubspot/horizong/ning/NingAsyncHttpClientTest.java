package com.hubspot.horizong.ning;

import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.ExpectedHttpResponse;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.TestServer;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import org.assertj.core.util.Closeables;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.hubspot.horizon.Assertions.assertThat;


public class NingAsyncHttpClientTest {
  private static final TestServer testServer = new TestServer();

  private AsyncHttpClient httpClient;

  @BeforeClass
  public static void start() throws Exception {
    testServer.start();
  }

  @AfterClass
  public static void stop() throws Exception {
    testServer.stop();
  }

  @After
  public void cleanup() {
    Closeables.closeQuietly(httpClient);
  }

  @Test
  public void itWorksWithHttp() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("".getBytes()).hasRetries(0);
  }

  @Test
  public void itWorksWithHttps() throws Exception {
    httpClient = new NingAsyncHttpClient(HttpConfig.newBuilder().setAcceptAllSSL(true).build());

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpsUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("".getBytes()).hasRetries(0);
  }

  @Test
  public void itRetriesServerErrors() throws Exception {
    httpClient = new NingAsyncHttpClient(HttpConfig.newBuilder().setMaxRetries(1).build());

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setStatusCode(500).build())
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(500).hasBody("".getBytes()).hasRetries(1);
  }
}
