package com.hubspot.horizong.ning;

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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

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
  public void cleanup() throws IOException {
    httpClient.close();
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

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
  }

  @Test
  public void itWorksWithHttps() throws Exception {
    httpClient = new NingAsyncHttpClient(HttpConfig.newBuilder().setSSLConfig(SSLConfig.acceptAll()).build());

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpsUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0);
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

    assertThat(response).hasStatusCode(500).hasBody("").hasRetries(1);
  }

  @Test
  public void itCompressesWithGzip() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .setCompression(Compression.GZIP)
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0).wasGzipCompressed();
  }

  @Test
  public void itCompressesWithSnappy() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().build())
            .setCompression(Compression.SNAPPY)
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("").hasRetries(0).wasSnappyCompressed();
  }

  @Test
  public void itReturnsCorrectResponseBody() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setBody("test").build())
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0);
  }

  @Test
  public void itDecompressesWithGzip() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setBody("test").setCompression(Compression.GZIP).build())
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0).isGzipCompressed();
  }

  @Test
  public void itDecompressesWithSnappy() throws Exception {
    httpClient = new NingAsyncHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.POST)
            .setUrl(testServer.baseHttpUrl())
            .setBody(ExpectedHttpResponse.newBuilder().setBody("test").setCompression(Compression.SNAPPY).build())
            .build();
    HttpResponse response = httpClient.execute(request).get();

    assertThat(response).hasStatusCode(200).hasBody("test").hasRetries(0).isSnappyCompressed();
  }
}
