package com.hubspot.horizong.ning;

import static com.hubspot.horizon.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.Compression;
import com.hubspot.horizon.ExpectedHttpResponse;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.SSLConfig;
import com.hubspot.horizon.TestServer;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class NingAsyncHttpClientTest {

  private static final TestServer TEST_SERVER = new TestServer();
  private static final String INVALID_SOCKS_HOST = "invalidSocksHost";

  private AsyncHttpClient httpClient;

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
  public void itWorksWithHttp() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
  }

  @Test
  public void shouldFailIfInvalidSocksProxyIsConfiguredForHttp() throws Exception {
    httpClient =
      new NingAsyncHttpClient(
        HttpConfig.newBuilder().setSocksProxyHost(INVALID_SOCKS_HOST).build()
      );

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    assertThatThrownBy(() -> httpClient.execute(request).get())
      .isExactlyInstanceOf(ExecutionException.class)
      .hasMessageContaining(INVALID_SOCKS_HOST)
      .hasCauseExactlyInstanceOf(UnknownHostException.class);
  }

  @Test
  @Ignore
  public void itWorksWithHttps() throws Exception {
    httpClient =
      new NingAsyncHttpClient(
        HttpConfig.newBuilder().setSSLConfig(SSLConfig.acceptAll()).build()
      );

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpsUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
  }

  @Test
  public void shouldFailIfInvalidSocksProxyIsConfiguredForHttps() throws Exception {
    httpClient =
      new NingAsyncHttpClient(
        HttpConfig
          .newBuilder()
          .setSSLConfig(SSLConfig.acceptAll())
          .setSocksProxyHost(INVALID_SOCKS_HOST)
          .build()
      );

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .build();
    assertThatThrownBy(() -> httpClient.execute(request).get())
      .isExactlyInstanceOf(ExecutionException.class)
      .hasMessageContaining(INVALID_SOCKS_HOST)
      .hasCauseExactlyInstanceOf(UnknownHostException.class);
  }

  @Test
  public void itRetriesServerErrors() throws Exception {
    httpClient =
      new NingAsyncHttpClient(HttpConfig.newBuilder().setMaxRetries(1).build());

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setStatusCode(500).build())
      .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(500).hasBody("").hasRetries(1);
  }

  @Test
  public void itCompressesWithGzip() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .setCompression(Compression.GZIP)
      .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0).wasGzipCompressed();
  }

  @Test
  public void itCompressesWithSnappy() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().build())
      .setCompression(Compression.SNAPPY)
      .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response)
      .hasStatusCode(200)
      .hasBody("")
      .hasRetries(0)
      .wasSnappyCompressed();
  }

  @Test
  public void itReturnsCorrectResponseBody() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.POST)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setBody("test").build())
      .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0);
  }

  @Test
  public void itDecompressesWithGzip() throws Exception {
    httpClient = new NingAsyncHttpClient();

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
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response)
      .hasStatusCode(200)
      .hasBody("test")
      .hasRetries(0)
      .isGzipCompressed();
  }

  @Test
  public void itDecompressesWithSnappy() throws Exception {
    httpClient = new NingAsyncHttpClient();

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
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response)
      .hasStatusCode(200)
      .hasBody("test")
      .hasRetries(0)
      .isSnappyCompressed();
  }

  @Test
  public void itSupportsDeleteWithBody() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest
      .newBuilder()
      .setMethod(Method.DELETE)
      .setUrl(TEST_SERVER.baseHttpUrl())
      .setBody(ExpectedHttpResponse.newBuilder().setBody("test").build())
      .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0);
  }

  @Test
  public void itDoesntLeakThreads() throws Exception {
    httpClient = new NingAsyncHttpClient();

    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    int initialThreadCount = threadMXBean.getThreadCount();
    for (int i = 0; i < 100; i++) {
      new NingAsyncHttpClient().close();
    }
    int finalThreadCount = threadMXBean.getThreadCount();

    assertThat(finalThreadCount - initialThreadCount).isLessThan(10);
  }
}
