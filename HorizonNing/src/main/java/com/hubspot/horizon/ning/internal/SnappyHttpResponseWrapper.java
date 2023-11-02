package com.hubspot.horizon.ning.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.hubspot.horizon.Headers;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.internal.AbstractHttpResponse;
import java.io.IOException;
import java.io.InputStream;
import org.xerial.snappy.SnappyInputStream;

public class SnappyHttpResponseWrapper extends AbstractHttpResponse {

  private final AbstractHttpResponse delegate;
  private final InputStream wrapped;

  public SnappyHttpResponseWrapper(AbstractHttpResponse delegate) throws IOException {
    this.delegate = Preconditions.checkNotNull(delegate);
    this.wrapped = new SnappyInputStream(delegate.getAsInputStream());
  }

  @Override
  public ObjectMapper getObjectMapper() {
    return delegate.getObjectMapper();
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
  public Headers getHeaders() {
    return delegate.getHeaders();
  }

  @Override
  public InputStream getAsInputStream() {
    return wrapped;
  }
}
