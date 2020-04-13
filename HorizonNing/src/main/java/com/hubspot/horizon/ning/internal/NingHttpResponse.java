package com.hubspot.horizon.ning.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.asynchttpclient.shaded.Response;
import org.asynchttpclient.shaded.io.netty.handler.codec.http.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.horizon.Header;
import com.hubspot.horizon.Headers;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.internal.AbstractHttpResponse;

public class NingHttpResponse extends AbstractHttpResponse {
  private final HttpRequest request;
  private final int statusCode;
  private final Headers headers;
  private final InputStream responseStream;
  private final ObjectMapper mapper;

  public NingHttpResponse(HttpRequest request, Response ningResponse, ObjectMapper mapper) throws IOException {
    this.request = request;
    this.statusCode = ningResponse.getStatusCode();
    this.headers = convertHeaders(ningResponse.getHeaders());
    this.responseStream = ningResponse.getResponseBodyAsStream();
    this.mapper = mapper;
  }

  private Headers convertHeaders(HttpHeaders ningHeaders) {
    List<Header> headers = new ArrayList<Header>();

    for (Entry<String, String> ningHeader : ningHeaders) {
      headers.add(new Header(ningHeader.getKey(), ningHeader.getValue()));
    }

    return new Headers(headers);
  }

  @Override
  public ObjectMapper getObjectMapper() {
    return mapper;
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
