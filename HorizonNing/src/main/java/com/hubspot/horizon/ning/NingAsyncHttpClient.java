package com.hubspot.horizon.ning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import com.ning.http.client.AsyncHttpProviderConfig;
import com.ning.http.client.Request;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.jboss.netty.util.Timer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NingAsyncHttpClient implements AsyncHttpClient {
  private static final ExecutorService BOSS_EXECUTOR = newExecutor("NioBoss");

  static {
    ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);
  }

  private final com.ning.http.client.AsyncHttpClient ningClient;
  private final NingHttpRequestConverter requestConverter;
  private final Options defaultOptions;
  private final ObjectMapper mapper;

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
            .setHostnameVerifier(new NingHostnameVerifier(config.getSSLConfig()))
            .setSSLContext(NingSSLContext.forConfig(config.getSSLConfig()))
            .setAsyncHttpClientProviderConfig(newAsyncProviderConfig())
            .setUserAgent(config.getUserAgent())
            .setCompressionEnabled(true)
            .setIOThreadMultiplier(1)
            .build();

    this.ningClient = new com.ning.http.client.AsyncHttpClient(ningConfig);
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

  private ListenableFuture<HttpResponse> internalExecute(HttpRequest request, Options options, Callback callback) {
    Preconditions.checkNotNull(request);
    Preconditions.checkNotNull(options);
    Preconditions.checkNotNull(callback);

    NingRetryHandler retryHandler = new NingRetryHandler(defaultOptions.mergeFrom(options));
    NingFuture future = new NingFuture(callback);

    final NingCompletionHandler completionHandler = new NingCompletionHandler(request, future, retryHandler, mapper);
    final Request ningRequest = requestConverter.convert(request);
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

  private static AsyncHttpProviderConfig<?, ?> newAsyncProviderConfig() {
    NettyAsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();
    nettyConfig.addProperty(NettyAsyncHttpProviderConfig.SOCKET_CHANNEL_FACTORY, newSocketChannelFactory());
    nettyConfig.setNettyTimer(newTimer());
    return nettyConfig;
  }

  private static NioClientSocketChannelFactory newSocketChannelFactory() {
    NioClientBossPool bossPool = new NioClientBossPool(BOSS_EXECUTOR, 1, newTimer(), ThreadNameDeterminer.CURRENT);
    NioWorkerPool workerPool = new NioWorkerPool(newExecutor("NioWorker"), 5, ThreadNameDeterminer.CURRENT);
    return new NioClientSocketChannelFactory(bossPool, workerPool);
  }

  private static ExecutorService newExecutor(String qualifier) {
    return Executors.newCachedThreadPool(newThreadFactory(qualifier));
  }

  private static Timer newTimer() {
    return new HashedWheelTimer(newThreadFactory("HashedWheelTimer"));
  }

  private static ThreadFactory newThreadFactory(String qualifier) {
    String nameFormat = "NingAsyncHttpClient-" + qualifier + "-%d";
    return new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build();
  }

  @Override
  public void close() throws IOException {
    ningClient.close();
  }
}
