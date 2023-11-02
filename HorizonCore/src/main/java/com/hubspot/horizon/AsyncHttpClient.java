package com.hubspot.horizon;

import com.google.common.util.concurrent.ListenableFuture;
import com.hubspot.horizon.HttpRequest.Options;
import java.io.Closeable;

public interface AsyncHttpClient extends Closeable {
  ListenableFuture<HttpResponse> execute(HttpRequest request);
  ListenableFuture<HttpResponse> execute(HttpRequest request, Options options);

  void execute(HttpRequest request, Callback callback);
  void execute(HttpRequest request, Options options, Callback callback);

  interface Callback {
    void completed(HttpResponse response);
    void failed(Exception e);
  }
}
