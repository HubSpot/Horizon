package com.hubspot.horizon.ning.internal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Options;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.RetryHelper;
import com.hubspot.horizon.RetryStrategy;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class NingRetryHandler implements RetryStrategy {
  private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
          .setDaemon(true)
          .setNameFormat("NingAsyncHttpClient-Retry-%d")
          .build();

  private static final ScheduledExecutorService RETRY_EXECUTOR = Executors.newScheduledThreadPool(5, THREAD_FACTORY);

  private final AtomicReference<Runnable> retryRunnable;
  private final Options options;
  private final AtomicInteger currentRetries;

  public NingRetryHandler(Options options) {
    this.retryRunnable = new AtomicReference<Runnable>();
    this.options = Preconditions.checkNotNull(options);
    this.currentRetries = new AtomicInteger();
  }

  public void setRetryRunnable(Runnable runnable) {
    retryRunnable.set(Preconditions.checkNotNull(runnable));
  }

  public void retry() {
    RETRY_EXECUTOR.schedule(retryRunnable(), computeBackoff(currentRetries.incrementAndGet()), TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean shouldRetry(HttpRequest request, HttpResponse response) {
    return retriesRemaining() && options.getRetryStrategy().shouldRetry(request, response);
  }

  @Override
  public boolean shouldRetry(HttpRequest request, IOException exception) {
    return retriesRemaining() && options.getRetryStrategy().shouldRetry(request, exception);
  }

  private int computeBackoff(int retries) {
    return RetryHelper.computeBackoff(options, retries);
  }

  private boolean retriesRemaining() {
    return currentRetries.get() < options.getMaxRetries();
  }

  private Runnable retryRunnable() {
    return Preconditions.checkNotNull(retryRunnable.get());
  }
}
