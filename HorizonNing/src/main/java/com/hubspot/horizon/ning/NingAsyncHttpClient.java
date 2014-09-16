package com.hubspot.horizon.ning;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Options;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.ning.internal.AcceptEncodingRequestFilter;
import com.hubspot.horizon.ning.internal.EmptyCallback;
import com.hubspot.horizon.ning.internal.NingCompletionHandler;
import com.hubspot.horizon.ning.internal.NingFuture;
import com.hubspot.horizon.ning.internal.NingHostnameVerifier;
import com.hubspot.horizon.ning.internal.NingHttpRequestConverter;
import com.hubspot.horizon.ning.internal.NingRetryHandler;
import com.hubspot.horizon.ning.internal.NingSSLContext;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Request;
import com.ning.http.client.extra.ThrottleRequestFilter;

import java.io.IOException;

public class NingAsyncHttpClient implements AsyncHttpClient {
  private final com.ning.http.client.AsyncHttpClient ningClient;
  private final Options defaultOptions;

  public NingAsyncHttpClient() {
    this(HttpConfig.newBuilder().build());
  }

  public NingAsyncHttpClient(HttpConfig config) {
    Preconditions.checkNotNull(config);

    AsyncHttpClientConfig ningConfig = new AsyncHttpClientConfig.Builder()
            .addRequestFilter(new ThrottleRequestFilter(config.getMaxConnections()))
            .addRequestFilter(new AcceptEncodingRequestFilter())
            .setMaximumConnectionsPerHost(config.getMaxConnectionsPerHost())
            .setConnectionTimeoutInMs(config.getConnectTimeoutMillis())
            .setRequestTimeoutInMs(config.getRequestTimeoutMillis())
            .setMaximumNumberOfRedirects(config.getMaxRedirects())
            .setFollowRedirects(config.isFollowRedirects())
            .setHostnameVerifier(NingHostnameVerifier.forConfig(config))
            .setSSLContext(NingSSLContext.forConfig(config))
            .setUserAgent(config.getUserAgent())
            .setCompressionEnabled(true)
            .setIOThreadMultiplier(1)
            .build();

    this.ningClient = new com.ning.http.client.AsyncHttpClient(ningConfig);
    this.defaultOptions = config.getOptions();
  }

  @Override
  public ListenableFuture<HttpResponse> execute(HttpRequest request) {
    return execute(Preconditions.checkNotNull(request), Options.DEFAULT);
  }

  @Override
  public ListenableFuture<HttpResponse> execute(HttpRequest request, Options options) {
    return internalExecute(request, options, EmptyCallback.INSTANCE);
  }

  @Override
  public void execute(HttpRequest request, Callback callback) {
    execute(Preconditions.checkNotNull(request), Options.DEFAULT, callback);
  }

  @Override
  public void execute(HttpRequest request, Options options, Callback callback) {
    internalExecute(request, options, callback);
  }

  private ListenableFuture<HttpResponse> internalExecute(HttpRequest request, Options options, Callback callback) {
    Preconditions.checkNotNull(request);
    Preconditions.checkNotNull(options);
    Preconditions.checkNotNull(callback);

    NingRetryHandler retryHandler = new NingRetryHandler(defaultOptions.mergeFrom(options));
    NingFuture future = new NingFuture(callback);

    final NingCompletionHandler completionHandler = new NingCompletionHandler(request, future, retryHandler);
    final Request ningRequest = NingHttpRequestConverter.convert(request);
    Runnable runnable = new Runnable() {

      @Override
      public void run() {
        try {
          ningClient.executeRequest(ningRequest, completionHandler);
        } catch (Exception e) {
          completionHandler.onThrowable(e);
        }
      }
    };
    retryHandler.setRetryRunnable(runnable);

    runnable.run();
    return future;
  }

  @Override
  public void close() throws IOException {
    ningClient.close();
  }
}
