package com.hubspot.horizon;

import com.google.common.util.concurrent.ListenableFuture;
import com.hubspot.horizon.HttpRequest.Options;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Closeable;

@ParametersAreNonnullByDefault
public interface AsyncHttpClient extends Closeable {
  @Nonnull ListenableFuture<HttpResponse> execute(HttpRequest request);
  @Nonnull ListenableFuture<HttpResponse> execute(HttpRequest request, Options options);

  void execute(HttpRequest request, Callback callback);
  void execute(HttpRequest request, Options options, Callback callback);

  @ParametersAreNonnullByDefault
  interface Callback {
    void completed(HttpResponse response);
    void failed(Exception e);
    void cancelled();
  }
}
