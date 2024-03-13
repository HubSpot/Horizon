package com.hubspot.horizon.ning;

import com.google.common.base.Preconditions;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Options;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.HttpRuntimeException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class NingHttpClient implements HttpClient {

  private final NingAsyncHttpClient delegate;

  public NingHttpClient() {
    this(HttpConfig.newBuilder().build());
  }

  public NingHttpClient(HttpConfig config) {
    this.delegate = new NingAsyncHttpClient(Preconditions.checkNotNull(config));
  }

  @Override
  public HttpResponse execute(HttpRequest request) throws HttpRuntimeException {
    return execute(Preconditions.checkNotNull(request), Options.DEFAULT);
  }

  @Override
  public HttpResponse execute(HttpRequest request, Options options)
    throws HttpRuntimeException {
    Preconditions.checkNotNull(request);
    Preconditions.checkNotNull(options);

    try {
      return delegate.execute(request, options).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new HttpRuntimeException(e);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof HttpRuntimeException) {
        throw (HttpRuntimeException) e.getCause();
      } else {
        throw new HttpRuntimeException(e.getCause());
      }
    }
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }
}
