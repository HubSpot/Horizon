package com.hubspot.horizon;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.hubspot.horizon.HttpRequest.Options;
import java.io.Closeable;

public interface AsyncHttpClient extends Closeable {
  default ListenableFuture<HttpResponse> execute(HttpRequest request) {
    return execute(request, Options.DEFAULT);
  }

  default ListenableFuture<HttpResponse> execute(HttpRequest request, Options options) {
    SettableFuture<HttpResponse> responseFuture = SettableFuture.create();

    try {
      execute(
        request,
        options,
        new Callback() {
          @Override
          public void completed(HttpResponse response) {
            responseFuture.set(response);
          }

          @Override
          public void failed(Exception e) {
            responseFuture.setException(e);
          }
        }
      );
    } catch (HttpRuntimeException e) {
      responseFuture.setException(e);
    }

    return responseFuture;
  }

  default void execute(HttpRequest request, Callback callback) {
    execute(request, Options.DEFAULT, callback);
  }

  void execute(HttpRequest request, Options options, Callback callback);

  interface Callback {
    void completed(HttpResponse response);
    void failed(Exception e);
  }
}
