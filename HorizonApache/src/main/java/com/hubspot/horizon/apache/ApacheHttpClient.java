package com.hubspot.horizon.apache;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Options;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.HttpRuntimeException;
import com.hubspot.horizon.RetryStrategy;
import com.hubspot.horizon.apache.internal.AcceptAllSSLSocketFactory;
import com.hubspot.horizon.apache.internal.ApacheHttpRequestConverter;
import com.hubspot.horizon.apache.internal.ApacheHttpResponse;
import com.hubspot.horizon.apache.internal.CachedHttpResponse;
import com.hubspot.horizon.apache.internal.DefaultHeadersRequestInterceptor;
import com.hubspot.horizon.apache.internal.KeepAliveWithDefaultStrategy;
import com.hubspot.horizon.apache.internal.LenientRedirectStrategy;
import com.hubspot.horizon.apache.internal.SnappyContentEncodingResponseInterceptor;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApacheHttpClient implements HttpClient {
  private static final Logger LOG = LoggerFactory.getLogger(ApacheHttpClient.class);

  private final org.apache.http.client.HttpClient apacheClient;
  private final HttpConfig config;
  private final Options defaultOptions;
  private final Timer timer;

  public ApacheHttpClient() {
    this(HttpConfig.newBuilder().build());
  }

  public ApacheHttpClient(HttpConfig config) {
    Preconditions.checkNotNull(config);

    PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
    connectionManager.setMaxTotal(config.getMaxConnections());
    connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());

    DefaultHttpClient apacheClient = new DefaultHttpClient(connectionManager);

    apacheClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectTimeoutMillis());
    apacheClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getRequestTimeoutMillis());
    apacheClient.getParams().setIntParameter(ClientPNames.MAX_REDIRECTS, config.getMaxRedirects());
    apacheClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, config.isFollowRedirects());
    apacheClient.getParams().setBooleanParameter(ClientPNames.REJECT_RELATIVE_REDIRECT, config.isRejectRelativeRedirects());

    apacheClient.setRedirectStrategy(new LenientRedirectStrategy());
    apacheClient.setKeepAliveStrategy(new KeepAliveWithDefaultStrategy(config.getDefaultKeepAliveMillis()));
    apacheClient.addRequestInterceptor(new DefaultHeadersRequestInterceptor(config));
    apacheClient.addResponseInterceptor(new SnappyContentEncodingResponseInterceptor());
    if (config.isAcceptAllSSL()) {
      SSLSocketFactory acceptAllSSLSocketFactory = AcceptAllSSLSocketFactory.newInstance();
      Scheme acceptAllSSLScheme = new Scheme("https", 443, acceptAllSSLSocketFactory);
      apacheClient.getConnectionManager().getSchemeRegistry().register(acceptAllSSLScheme);
    }

    this.apacheClient = apacheClient;
    this.config = config;
    this.defaultOptions = config.getOptions();
    this.timer = new Timer("http-request-timeout", true);
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

    HttpUriRequest apacheRequest = ApacheHttpRequestConverter.convert(request);
    org.apache.http.HttpResponse apacheResponse = null;
    AtomicBoolean timedOut = new AtomicBoolean(false);
    try {
      TimerTask timeoutTask = setupTimeoutTask(apacheRequest, timedOut);

      final HttpResponse response;
      try {
        apacheResponse = apacheClient.execute(apacheRequest);
        response = CachedHttpResponse.from(new ApacheHttpResponse(request, apacheResponse));
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
      Thread.sleep(computeBackoff(options, retries));
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

  private int computeBackoff(Options options, int retries) {
    int initialBackoff = options.getInitialRetryBackoffMillis();
    int computedBackoff = nextInt(initialBackoff / 4) + (initialBackoff * retries * retries);

    return Math.min(computedBackoff, options.getMaxRetryBackoffMillis());
  }

  private static int nextInt(int n) {
    return ThreadLocalRandom.current().nextInt(Math.max(n, 1));
  }

  @Override
  public void close() throws IOException {
    apacheClient.getConnectionManager().shutdown();
  }
}
