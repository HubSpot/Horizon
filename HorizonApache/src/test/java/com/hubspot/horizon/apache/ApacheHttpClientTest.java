package com.hubspot.horizon.apache;

import com.hubspot.horizon.ExpectedHttpResponse;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.TestServer;
import org.assertj.core.util.Closeables;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.hubspot.horizon.Assertions.assertThat;


public class ApacheHttpClientTest {
  private static final TestServer testServer = new TestServer();

  private HttpClient httpClient;

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
  public void itWorksWithHttp() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("".getBytes()).hasRetries(0);
  }

  @Test
  public void itWorksWithHttps() {
    httpClient = new ApacheHttpClient(HttpConfig.newBuilder().setAcceptAllSSL(true).build());

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpsUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("".getBytes()).hasRetries(0);
  }

  @Test
  public void itRetriesServerErrors() {
    httpClient = new ApacheHttpClient(HttpConfig.newBuilder().setMaxRetries(1).build());

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setStatusCode(500).build())
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(500).hasBody("".getBytes()).hasRetries(1);
  }
}
