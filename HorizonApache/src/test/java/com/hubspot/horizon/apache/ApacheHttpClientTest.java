package com.hubspot.horizon.apache;

import com.hubspot.horizon.Compression;
import com.hubspot.horizon.ExpectedHttpResponse;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.HttpRuntimeException;
import com.hubspot.horizon.SSLConfig;
import com.hubspot.horizon.TestServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;

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
  public void cleanup() throws IOException {
    httpClient.close();
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

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
  }

  @Test(expected = HttpRuntimeException.class)
  public void shouldFailIfInvalidSocksProxyIsConfigured() {
    httpClient = new ApacheHttpClient("invalidSocksHost");

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .build();
    httpClient.execute(request);
  }

  @Test
  public void itWorksWithHttps() {
    httpClient = new ApacheHttpClient(HttpConfig.newBuilder().setSSLConfig(SSLConfig.acceptAll()).build());

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpsUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
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

    assertThat(response).hasStatusCode(500).hasBody("").hasRetries(1);
  }

  @Test
  public void itCompressesWithGzip() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .setCompression(Compression.GZIP)
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0).wasGzipCompressed();
  }

  @Test
  public void itCompressesWithSnappy() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .setCompression(Compression.SNAPPY)
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0).wasSnappyCompressed();
  }

  @Test
  public void itReturnsCorrectResponseBody() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setBody("test").build())
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0);
  }

  @Test
  public void itDecompressesWithGzip() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setBody("test").setCompression(Compression.GZIP).build())
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0).isGzipCompressed();
  }

  @Test
  public void itDecompressesWithSnappy() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setBody("test").setCompression(Compression.SNAPPY).build())
            .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0).isSnappyCompressed();
  }

  @Test
  public void itSupportsDeleteWithBody() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
        .setMethod(Method.DELETE)
        .setUrl(testServer.baseHttpUrl())
        .setBody(ExpectedHttpResponse.newBuilder().setBody("test").build())
        .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0);
  }
}
