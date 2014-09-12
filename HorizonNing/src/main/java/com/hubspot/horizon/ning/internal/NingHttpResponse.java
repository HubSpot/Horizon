package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.internal.AbstractHttpResponse;
import com.ning.http.client.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class NingHttpResponse extends AbstractHttpResponse {
  private final HttpRequest request;
  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final InputStream responseStream;

  public NingHttpResponse(HttpRequest request, Response ningResponse) throws IOException {
    this.request = request;
    this.statusCode = ningResponse.getStatusCode();
    this.headers = ningResponse.getHeaders();
    this.responseStream = ningResponse.getResponseBodyAsStream();
  }

  @Nonnull
  @Override
  public HttpRequest getRequest() {
    return request;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Nonnull
  @Override
  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  @Nonnull
  @Override
  public InputStream getAsInputStream() {
    return responseStream;
  }
}
