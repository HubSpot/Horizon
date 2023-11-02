package com.hubspot.horizon;

import com.hubspot.horizon.HttpRequest.Options;
import java.util.Random;

public final class RetryHelper {

  private static final ThreadLocal<Random> RANDOM = new ThreadLocal<Random>() {
    @Override
    protected Random initialValue() {
      return new Random();
    }
  };

  private RetryHelper() {
    throw new AssertionError();
  }

  public static int computeBackoff(Options options, int retries) {
    int initialBackoff = options.getInitialRetryBackoffMillis();
    int computedBackoff =
      nextInt(initialBackoff / 4) + (initialBackoff * retries * retries);

    return Math.min(computedBackoff, options.getMaxRetryBackoffMillis());
  }

  private static int nextInt(int n) {
    return RANDOM.get().nextInt(Math.max(n, 1));
  }
}
