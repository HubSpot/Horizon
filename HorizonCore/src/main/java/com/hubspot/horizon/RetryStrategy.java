package com.hubspot.horizon;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

public interface RetryStrategy {
  boolean shouldRetry(HttpRequest request, HttpResponse response);
  boolean shouldRetry(HttpRequest request, IOException exception);

  public RetryStrategy DEFAULT = new RetryStrategy() {
    @Override
    public boolean shouldRetry(HttpRequest request, HttpResponse response) {
      return response.isServerError();
    }

    @Override
    public boolean shouldRetry(HttpRequest request, IOException exception) {
      // avoid Apache dependency
      if ("NoHttpResponseException".equals(exception.getClass().getSimpleName())) {
        return true;
      }
      if (exception instanceof InterruptedIOException) {
        return false;
      }
      if (exception instanceof UnknownHostException) {
        return false;
      }
      if (exception instanceof ConnectException) {
        return false;
      }
      if (exception instanceof SSLHandshakeException) {
        return false;
      }
      if (exception instanceof SSLPeerUnverifiedException) {
        return false;
      }
      if (exception.getCause() instanceof TimeoutException) {
        return false;
      }

      return true;
    }
  };

  public RetryStrategy NEVER_RETRY = new RetryStrategy() {
    @Override
    public boolean shouldRetry(HttpRequest request, HttpResponse response) {
      return false;
    }

    @Override
    public boolean shouldRetry(HttpRequest request, IOException exception) {
      return false;
    }
  };
}
