package com.hubspot.horizon.ning.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.internal.AbstractHttpResponse;
import java.io.IOException;
import org.asynchttpclient.shaded.AsyncCompletionHandler;
import org.asynchttpclient.shaded.Response;

public class NingCompletionHandler extends AsyncCompletionHandler<HttpResponse> {

  private final HttpRequest request;
  private final NingFuture future;
  private final NingRetryHandler retryHandler;
  private final ObjectMapper mapper;

  public NingCompletionHandler(
    HttpRequest request,
    NingFuture future,
    NingRetryHandler retryHandler,
    ObjectMapper mapper
  ) {
    this.request = request;
    this.future = future;
    this.retryHandler = retryHandler;
    this.mapper = mapper;
  }

  @Override
  public HttpResponse onCompleted(Response ningResponse) throws Exception {
    AbstractHttpResponse response = new NingHttpResponse(request, ningResponse, mapper);
    if ("snappy".equals(ningResponse.getHeader(HttpHeaders.CONTENT_ENCODING))) {
      response = new SnappyHttpResponseWrapper(response);
    }
    if (retryHandler.shouldRetry(request, response)) {
      retryHandler.retry();
    } else {
      future.setNonnull(response);
    }
    return response;
  }

  @Override
  public void onThrowable(Throwable t) {
    IOException e = t instanceof IOException ? (IOException) t : new IOException(t);
    if (retryHandler.shouldRetry(request, e)) {
      retryHandler.retry();
    } else {
      future.setException(e);
    }
  }
}
