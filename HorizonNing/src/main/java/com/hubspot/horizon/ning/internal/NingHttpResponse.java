package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.Header;
import com.hubspot.horizon.Headers;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.internal.AbstractHttpResponse;
import com.ning.http.client.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NingHttpResponse extends AbstractHttpResponse {
  private final HttpRequest request;
  private final int statusCode;
  private final Headers headers;
  private final InputStream responseStream;

  public NingHttpResponse(HttpRequest request, Response ningResponse) throws IOException {
    this.request = request;
    this.statusCode = ningResponse.getStatusCode();
    this.headers = convertHeaders(ningResponse.getHeaders());
    this.responseStream = ningResponse.getResponseBodyAsStream();
  }

  private Headers convertHeaders(Map<String, List<String>> ningHeaders) {
    List<Header> headers = new ArrayList<Header>();

    for (Entry<String, List<String>> ningHeader : ningHeaders.entrySet()) {
      String name = ningHeader.getKey();

      for (String value : ningHeader.getValue()) {
        headers.add(new Header(name, value));
      }
    }

    return new Headers(headers);
  }

  @Override
  public HttpRequest getRequest() {
    return request;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public Headers getHeaders() {
    return headers;
  }

  @Override
  public InputStream getAsInputStream() {
    return responseStream;
  }
}
