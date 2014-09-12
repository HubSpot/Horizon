package com.hubspot.horizon.apache;

import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.TestServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApacheHttpClientTest {
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
  public void testHttp() {
    HttpClient httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder().setUrl("http://localhost:" + testServer.getHttpPort()).build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getAsString()).isEqualTo("<h1>Hello World</h1>");
  }

  @Test
  public void testHttps() {
    HttpClient httpClient = new ApacheHttpClient(HttpConfig.newBuilder().setAcceptAllSSL(true).build());

    HttpRequest request = HttpRequest.newBuilder().setUrl("https://localhost:" + testServer.getHttpsPort()).build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getAsString()).isEqualTo("<h1>Hello World</h1>");
  }
}
