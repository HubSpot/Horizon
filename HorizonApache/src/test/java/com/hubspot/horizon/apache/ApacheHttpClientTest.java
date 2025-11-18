package com.hubspot.horizon.apache;

import static com.hubspot.horizon.Assertions.assertThat;
import static com.hubspot.horizon.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.hubspot.horizon.Compression;
import com.hubspot.horizon.ExpectedHttpResponse;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpRequest.Options;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.HttpRuntimeException;
import com.hubspot.horizon.SSLConfig;
import com.hubspot.horizon.TestServer;
import java.io.IOException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ApacheHttpClientTest {

  private static final TestServer TEST_SERVER = new TestServer();
  private static final String INVALID_SOCKS_HOST = "invalidSocksHost";

  private HttpClient httpClient;

  @BeforeClass
  public static void start() throws Exception {
    TEST_SERVER.start();
  }

  @AfterClass
  public static void stop() throws Exception {
    TEST_SERVER.stop();
  }

  @After
  public void cleanup() throws IOException {
    httpClient.close();
  }

  @Test
  public void itWorksWithHttp() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
  }

  @Test
  public void shouldFailIfInvalidSocksProxyIsConfiguredforHttp() {
    httpClient =
      new ApacheHttpClient(
        HttpConfig
          .newBuilder()
          .setSocksProxyHost(INVALID_SOCKS_HOST)
          .setMaxRetries(0)
          .build()
      );

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    assertThatThrownBy(() -> httpClient.execute(request))
      .isExactlyInstanceOf(HttpRuntimeException.class)
      .hasMessageContaining(INVALID_SOCKS_HOST)
      .hasCauseExactlyInstanceOf(SocketException.class);
  }

  @Test
  public void itWorksWithHttps() {
    httpClient =
      new ApacheHttpClient(
        HttpConfig.newBuilder().setSSLConfig(SSLConfig.acceptAll()).build()
      );

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpsUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
  }

  @Test
  public void shouldFailIfInvalidSocksProxyIsConfiguredforHttps() {
    httpClient =
      new ApacheHttpClient(
        HttpConfig
          .newBuilder()
          .setSSLConfig(SSLConfig.acceptAll())
          .setSocksProxyHost(INVALID_SOCKS_HOST)
          .setMaxRetries(0)
          .build()
      );

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    assertThatThrownBy(() -> httpClient.execute(request))
      .isExactlyInstanceOf(HttpRuntimeException.class)
      .hasMessageContaining(INVALID_SOCKS_HOST)
      .hasCauseExactlyInstanceOf(SocketException.class);
  }

  @Test
  public void itRetriesServerErrors() {
    httpClient = new ApacheHttpClient(HttpConfig.newBuilder().setMaxRetries(1).build());

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setStatusCode(500).build())
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(500).hasBody("").hasRetries(1);
  }

  @Test
  public void itCompressesWithGzip() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .setCompression(Compression.GZIP)
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0).wasGzipCompressed();
  }

  @Test
  public void itCompressesWithSnappy() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .setCompression(Compression.SNAPPY)
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response)
      .hasStatusCode(200)
      .hasBody("")
      .hasRetries(0)
      .wasSnappyCompressed();
  }

  @Test
  public void itReturnsCorrectResponseBody() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setBody("test").build())
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0);
  }

  @Test
  public void itDecompressesWithGzip() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(
        ExpectedHttpResponse
          .newBuilder()
          .setBody("test")
          .setCompression(Compression.GZIP)
          .build()
      )
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response)
      .hasStatusCode(200)
      .hasBody("test")
      .hasRetries(0)
      .isGzipCompressed();
  }

  @Test
  public void itDecompressesWithSnappy() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(
        ExpectedHttpResponse
          .newBuilder()
          .setBody("test")
          .setCompression(Compression.SNAPPY)
          .build()
      )
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response)
      .hasStatusCode(200)
      .hasBody("test")
      .hasRetries(0)
      .isSnappyCompressed();
  }

  @Test
  public void itSupportsDeleteWithBody() {
    httpClient = new ApacheHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.DELETE)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setBody("test").build())
      .build();
    HttpResponse response = httpClient.execute(request);

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0);
  }

  @Test
  public void itRespectsGlobalRequestTimeout() {
    httpClient =
      new ApacheHttpClient(HttpConfig.newBuilder().setRequestTimeoutSeconds(1).build());

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setDelay(Duration.ofSeconds(2)).build())
      .build();
    Throwable t = catchThrowable(() -> httpClient.execute(request));

    assertThat(t).isNotNull().hasRootCauseInstanceOf(SocketTimeoutException.class);
  }

  @Test
  public void itRespectsPerRequestTimeoutWhenHigher() {
    httpClient =
      new ApacheHttpClient(HttpConfig.newBuilder().setRequestTimeoutSeconds(1).build());

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setDelay(Duration.ofSeconds(2)).build())
      .build();

    Options options = new Options();
    options.setRequestTimeoutSeconds(5);
    HttpResponse response = httpClient.execute(request, options);

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
  }

  @Test
  public void itRespectsPerRequestTimeoutWhenLower() {
    httpClient =
      new ApacheHttpClient(HttpConfig.newBuilder().setRequestTimeoutSeconds(5).build());

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setDelay(Duration.ofSeconds(2)).build())
      .build();

    Options options = new Options();
    options.setRequestTimeoutSeconds(1);
    Throwable t = catchThrowable(() -> httpClient.execute(request, options));

    assertThat(t).isNotNull().hasRootCauseInstanceOf(SocketTimeoutException.class);
  }
}
