package com.hubspot.horizon.ning.internal;

import com.google.common.base.Preconditions;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.internal.AbstractHttpResponse;
import org.xerial.snappy.SnappyInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class SnappyHttpResponseWrapper extends AbstractHttpResponse {
  private final HttpResponse delegate;
  private final InputStream wrapped;

  public SnappyHttpResponseWrapper(HttpResponse delegate) throws IOException {
    this.delegate = Preconditions.checkNotNull(delegate);
    this.wrapped = new SnappyInputStream(delegate.getAsInputStream());
  }

  @Override
  public HttpRequest getRequest() {
    return delegate.getRequest();
  }

  @Override
  public int getStatusCode() {
    return delegate.getStatusCode();
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return delegate.getHeaders();
  }

  @Override
  public InputStream getAsInputStream() {
    return wrapped;
  }
}
