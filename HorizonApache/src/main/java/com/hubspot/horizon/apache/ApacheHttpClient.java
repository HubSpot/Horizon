package com.hubspot.horizon.apache;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Options;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.HttpRuntimeException;
import com.hubspot.horizon.RetryHelper;
import com.hubspot.horizon.RetryStrategy;
import com.hubspot.horizon.apache.internal.ApacheHttpRequestConverter;
import com.hubspot.horizon.apache.internal.ApacheHttpResponse;
import com.hubspot.horizon.apache.internal.ApacheSSLSocketFactory;
import com.hubspot.horizon.apache.internal.CachedHttpResponse;
import com.hubspot.horizon.apache.internal.DefaultHeadersRequestInterceptor;
import com.hubspot.horizon.apache.internal.KeepAliveWithDefaultStrategy;
import com.hubspot.horizon.apache.internal.LenientRedirectStrategy;
import com.hubspot.horizon.apache.internal.ProxiedPlainConnectionSocketFactory;
import com.hubspot.horizon.apache.internal.ProxiedSSLSocketFactory;
import com.hubspot.horizon.apache.internal.SnappyContentEncodingResponseInterceptor;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApacheHttpClient implements HttpClient {
  private static final Logger LOG = LoggerFactory.getLogger(ApacheHttpClient.class);

  private final CloseableHttpClient apacheClient;
  private final ApacheHttpRequestConverter requestConverter;
  private final HttpConfig config;
  private final Options defaultOptions;
  private final Timer timer;

  public ApacheHttpClient() {
    this(HttpConfig.newBuilder().build());
  }

  public ApacheHttpClient(HttpConfig config) {
    Preconditions.checkNotNull(config);

    HttpClientBuilder builder = HttpClientBuilder.create();

    builder.setConnectionManager(createConnectionManager(config));
    builder.setRedirectStrategy(new LenientRedirectStrategy());
    builder.setKeepAliveStrategy(new KeepAliveWithDefaultStrategy(config.getDefaultKeepAliveMillis()));
    builder.setConnectionTimeToLive(config.getConnectionTtlMillis(), TimeUnit.MILLISECONDS);
    builder.addInterceptorFirst(new DefaultHeadersRequestInterceptor(config));
    builder.addInterceptorFirst(new SnappyContentEncodingResponseInterceptor());
    builder.setDefaultRequestConfig(createRequestConfig(config));
    builder.setDefaultSocketConfig(createSocketConfig(config));
    builder.disableContentCompression();

    this.apacheClient = builder.build();
    this.requestConverter = new ApacheHttpRequestConverter(config.getObjectMapper());
    this.config = config;
    this.defaultOptions = config.getOptions();
    this.timer = new Timer("http-request-timeout", true);
  }

  private HttpClientConnectionManager createConnectionManager(HttpConfig config) {
    Registry<ConnectionSocketFactory> registry = createSocketFactoryRegistry(config);
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
    connectionManager.setMaxTotal(config.getMaxConnections());
    connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());

    return connectionManager;
  }

  private Registry<ConnectionSocketFactory> createSocketFactoryRegistry(HttpConfig config) {
    RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.create();

    if (config.isSocksProxied()) {
      builder.register("http", ProxiedPlainConnectionSocketFactory.getSocketFactory());
      builder.register("https", ProxiedSSLSocketFactory.forConfig(config.getSSLConfig()));
    } else {
      builder.register("http", PlainConnectionSocketFactory.getSocketFactory());
      builder.register("https", ApacheSSLSocketFactory.forConfig(config.getSSLConfig()));
    }

    return builder.build();
  }

  private RequestConfig createRequestConfig(HttpConfig config) {
    return RequestConfig.custom()
            .setConnectionRequestTimeout(config.getConnectTimeoutMillis())
            .setConnectTimeout(config.getConnectTimeoutMillis())
            .setSocketTimeout(config.getRequestTimeoutMillis())
            .setRedirectsEnabled(config.isFollowRedirects())
            .setMaxRedirects(config.getMaxRedirects())
            .setRelativeRedirectsAllowed(config.isRejectRelativeRedirects())
            .build();
  }

  private SocketConfig createSocketConfig(HttpConfig config) {
    return SocketConfig.custom().setSoTimeout(config.getRequestTimeoutMillis()).build();
  }

  @Override
  public HttpResponse execute(HttpRequest request) throws HttpRuntimeException {
    return execute(Preconditions.checkNotNull(request), Options.DEFAULT);
  }

  @Override
  public HttpResponse execute(HttpRequest request, Options options) throws HttpRuntimeException {
    Preconditions.checkNotNull(request);
    Preconditions.checkNotNull(options);

    try {
      return executeWithRetries(request, defaultOptions.mergeFrom(options), 0);
    } catch (Exception e) {
      Throwables.propagateIfInstanceOf(e, HttpRuntimeException.class);
      throw new HttpRuntimeException(e);
    }
  }

  private HttpResponse executeWithRetries(HttpRequest request, Options options, int retries) throws IOException {
    int maxRetries = options.getMaxRetries();
    RetryStrategy retryStrategy = options.getRetryStrategy();

    HttpUriRequest apacheRequest = requestConverter.convert(request);
    org.apache.http.HttpResponse apacheResponse = null;
    AtomicBoolean timedOut = new AtomicBoolean(false);
    try {
      TimerTask timeoutTask = setupTimeoutTask(apacheRequest, timedOut);

      final HttpResponse response;
      try {
        if (config.isSocksProxied()) {
          InetSocketAddress socksaddr = new InetSocketAddress(config.getSocksProxyHost().get(), config.getSocksProxyPort());
          HttpClientContext context = HttpClientContext.create();
          context.setAttribute("socks.address", socksaddr);
          LOG.debug("Sending http request to {} via proxy @{}", request.getUrl(), config.getSocksProxyHost());
          apacheResponse = apacheClient.execute(apacheRequest, context);
        } else {
          apacheResponse = apacheClient.execute(apacheRequest);
        }
        response = CachedHttpResponse.from(new ApacheHttpResponse(request, apacheResponse, config.getObjectMapper()));
      } finally {
        // once this is done the timeout can be canceled
        timeoutTask.cancel();
      }

      if (retries < maxRetries && retryStrategy.shouldRetry(request, response)) {
        LOG.warn(String.format("Going to retry failed request to '%s' (Status: %d)", request.getUrl(), response.getStatusCode()));
      } else {
        return response;
      }
    } catch (ClientProtocolException e) {
      throw e;
    } catch (IOException e) {
      if (timedOut.get()) {
        close(apacheResponse);
        e = new IOException(new TimeoutException(String.format("Request to '%s' timed out", request.getUrl())));
      }

      if (retries < maxRetries && retryStrategy.shouldRetry(request, e)) {
        LOG.warn(String.format("Going to retry failed request to '%s'", request.getUrl()), e);
      } else {
        throw e;
      }
    }

    return backoffAndRetry(request, options, retries + 1);
  }

  private HttpResponse backoffAndRetry(HttpRequest request, Options options, int retries) throws IOException {
    try {
      Thread.sleep(RetryHelper.computeBackoff(options, retries));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new HttpRuntimeException(e);
    }

    return executeWithRetries(request, options, retries);
  }

  private TimerTask setupTimeoutTask(final HttpUriRequest request, final AtomicBoolean timedOut) {
    int delay = config.getConnectTimeoutMillis() + config.getRequestTimeoutMillis();
    TimerTask timeoutTask = new TimerTask() {

      @Override
      public void run() {
        timedOut.set(true);
        request.abort();
      }
    };
    timer.schedule(timeoutTask, delay);

    return timeoutTask;
  }

  private void close(@Nullable org.apache.http.HttpResponse response) {
    if (response != null) {
      try {
        // make sure these resources are returned
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          InputStream responseStream = entity.getContent();
          if (responseStream != null) {
            responseStream.close();
          }
        }
      } catch (Exception e) {
        LOG.warn("Error closing Apache response", e);
      }
    }
  }

  @Override
  public void close() throws IOException {
    apacheClient.close();
  }
}
