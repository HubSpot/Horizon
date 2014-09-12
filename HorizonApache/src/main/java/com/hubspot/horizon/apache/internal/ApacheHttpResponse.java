package com.hubspot.horizon.apache.internal;

import com.google.common.collect.Lists;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.zenith.internal.AbstractHttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApacheHttpResponse extends AbstractHttpResponse {
  private final HttpRequest request;
  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final InputStream responseStream;

  public ApacheHttpResponse(HttpRequest request, HttpResponse apacheResponse) throws IOException {
    this.request = request;
    this.statusCode = apacheResponse.getStatusLine().getStatusCode();
    this.headers = extractHeaders(apacheResponse);
    this.responseStream = extractResponseStream(apacheResponse);
  }

  private Map<String, List<String>> extractHeaders(HttpResponse apacheResponse) {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();

    for (Header header : apacheResponse.getAllHeaders()) {
      String name = header.getName();
      String value = header.getValue();

      if (headers.containsKey(name)) {
        headers.get(name).add(value);
      } else {
        headers.put(name, Lists.newArrayList(value));
      }
    }

    return headers;
  }

  private InputStream extractResponseStream(HttpResponse apacheResponse) throws IOException {
    HttpEntity entity = apacheResponse.getEntity();
    if (entity == null) {
      return EmptyInputStream.getInstance();
    } else {
      InputStream responseStream = entity.getContent();
      return responseStream == null ? EmptyInputStream.getInstance() : responseStream;
    }
  }

  @Override
  public @Nonnull HttpRequest getRequest() {
    return request;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public @Nonnull Map<String, List<String>> getHeaders() {
    return headers;
  }

  @Override
  public @Nonnull InputStream getAsInputStream() {
    return responseStream;
  }
}
