package com.hubspot.horizon.apache.internal;

import com.google.common.io.Closeables;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.internal.AbstractHttpResponse;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class CachedHttpResponse extends AbstractHttpResponse {
  private final HttpResponse delegate;
  private final byte[] responseBytes;

  private CachedHttpResponse(HttpResponse delegate) throws IOException {
    this.delegate = delegate;
    try {
      this.responseBytes = delegate.getAsBytes();
    } catch (RuntimeException e) {
      throw e.getCause() instanceof IOException ? (IOException) e.getCause() : new IOException(e);
    } finally {
      Closeables.closeQuietly(delegate.getAsInputStream());
    }
  }

  public static HttpResponse from(HttpResponse response) throws IOException {
    return response instanceof CachedHttpResponse ? response : new CachedHttpResponse(response);
  }

  @Override
  public @Nonnull HttpRequest getRequest() {
    return delegate.getRequest();
  }

  @Override
  public int getStatusCode() {
    return delegate.getStatusCode();
  }

  @Override
  public @Nonnull Map<String, List<String>> getHeaders() {
    return delegate.getHeaders();
  }

  @Override
  public @Nonnull byte[] getAsBytes() {
    return responseBytes;
  }

  @Override
  public @Nonnull InputStream getAsInputStream() {
    return new ByteArrayInputStream(responseBytes);
  }
}
