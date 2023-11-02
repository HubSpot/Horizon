package com.hubspot.horizon.ning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Options;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.ning.internal.AcceptEncodingRequestFilter;
import com.hubspot.horizon.ning.internal.DefaultHeadersRequestFilter;
import com.hubspot.horizon.ning.internal.EmptyCallback;
import com.hubspot.horizon.ning.internal.NingCompletionHandler;
import com.hubspot.horizon.ning.internal.NingFuture;
import com.hubspot.horizon.ning.internal.NingHttpRequestConverter;
import com.hubspot.horizon.ning.internal.NingRetryHandler;
import com.hubspot.horizon.ning.internal.NingSSLContext;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.asynchttpclient.shaded.AsyncHttpClientConfig;
import org.asynchttpclient.shaded.DefaultAsyncHttpClient;
import org.asynchttpclient.shaded.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.shaded.Request;
import org.asynchttpclient.shaded.filter.ThrottleRequestFilter;
import org.asynchttpclient.shaded.io.netty.channel.EventLoopGroup;
import org.asynchttpclient.shaded.io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.shaded.io.netty.util.HashedWheelTimer;
import org.asynchttpclient.shaded.io.netty.util.concurrent.DefaultThreadFactory;
import org.asynchttpclient.shaded.proxy.ProxyServer;
import org.asynchttpclient.shaded.proxy.ProxyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NingAsyncHttpClient implements AsyncHttpClient {

  private static final HashedWheelTimer TIMER = new HashedWheelTimer(
    newThreadFactory("NingAsyncHttpClient-Timer")
  );
  private static final Logger LOG = LoggerFactory.getLogger(NingAsyncHttpClient.class);

  private final org.asynchttpclient.shaded.AsyncHttpClient ningClient;
  private final NingHttpRequestConverter requestConverter;
  private final Options defaultOptions;
  private final ObjectMapper mapper;
  private final EventLoopGroup eventLoopGroup;

  public NingAsyncHttpClient() {
    this(HttpConfig.newBuilder().build());
  }

  public NingAsyncHttpClient(HttpConfig config) {
    Preconditions.checkNotNull(config);

    this.eventLoopGroup = newEventLoopGroup();

    DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();

    if (config.isSocksProxied()) {
      LOG.debug(
        "Client will be routed via SOCKS proxy {}:{}",
        config.getSocksProxyHost().get(),
        config.getSocksProxyPort()
      );
      ProxyServer proxyServer = new ProxyServer.Builder(
        config.getSocksProxyHost().get(),
        config.getSocksProxyPort()
      )
        .setProxyType(ProxyType.SOCKS_V5)
        .build();
      builder.setProxyServer(proxyServer);
    }

    AsyncHttpClientConfig ningConfig = builder
      .addRequestFilter(new ThrottleRequestFilter(config.getMaxConnections()))
      .addRequestFilter(new AcceptEncodingRequestFilter())
      .addRequestFilter(new DefaultHeadersRequestFilter(config))
      .setMaxConnectionsPerHost(config.getMaxConnectionsPerHost())
      .setConnectionTtl(config.getConnectionTtlMillis())
      .setConnectTimeout(config.getConnectTimeoutMillis())
      .setRequestTimeout(config.getRequestTimeoutMillis())
      .setReadTimeout(config.getRequestTimeoutMillis())
      .setMaxRedirects(config.getMaxRedirects())
      .setFollowRedirect(config.isFollowRedirects())
      .setSslContext(NingSSLContext.forConfig(config.getSSLConfig()))
      .setUserAgent(config.getUserAgent())
      .setEventLoopGroup(eventLoopGroup)
      .setNettyTimer(TIMER)
      .setMaxRequestRetry(0) // we handle retries ourselves
      .build();

    this.ningClient = new DefaultAsyncHttpClient(ningConfig);
    this.requestConverter = new NingHttpRequestConverter(config.getObjectMapper());
    this.defaultOptions = config.getOptions();
    this.mapper = config.getObjectMapper();
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

  private ListenableFuture<HttpResponse> internalExecute(
    HttpRequest request,
    Options options,
    Callback callback
  ) {
    Preconditions.checkNotNull(request);
    Preconditions.checkNotNull(options);
    Preconditions.checkNotNull(callback);

    NingRetryHandler retryHandler = new NingRetryHandler(
      defaultOptions.mergeFrom(options)
    );
    NingFuture future = new NingFuture(callback);

    final NingCompletionHandler completionHandler = new NingCompletionHandler(
      request,
      future,
      retryHandler,
      mapper
    );
    final Request ningRequest = requestConverter.convert(request, options);
    Runnable runnable = () -> {
      try {
        ningClient.executeRequest(ningRequest, completionHandler);
      } catch (RuntimeException e) {
        completionHandler.onThrowable(e);
      }
    };
    retryHandler.setRetryRunnable(runnable);

    runnable.run();
    return future;
  }

  private EventLoopGroup newEventLoopGroup() {
    ThreadFactory threadFactory = newThreadFactory("NingAsyncHttpClient");
    int workerThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4);

    return new NioEventLoopGroup(workerThreads, threadFactory);
  }

  private static ThreadFactory newThreadFactory(String name) {
    return new DefaultThreadFactory(name, true);
  }

  @Override
  public void close() throws IOException {
    try {
      ningClient.close();
    } finally {
      eventLoopGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
    }
  }
}
