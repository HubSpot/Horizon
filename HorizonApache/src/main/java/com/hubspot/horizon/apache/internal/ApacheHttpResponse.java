package com.hubspot.horizon.apache.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.horizon.Header;
import com.hubspot.horizon.Headers;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.internal.AbstractHttpResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class ApacheHttpResponse extends AbstractHttpResponse {

  private final HttpRequest request;
  private final int statusCode;
  private final Headers headers;
  private final InputStream responseStream;
  private final ObjectMapper mapper;

  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public ApacheHttpResponse(
    HttpRequest request,
    HttpResponse apacheResponse,
    ObjectMapper mapper
  ) throws IOException {
    this.request = request;
    this.statusCode = apacheResponse.getStatusLine().getStatusCode();
    this.headers = convertHeaders(apacheResponse.getAllHeaders());
    this.responseStream = extractResponseStream(apacheResponse);
    this.mapper = mapper;
  }

  private Headers convertHeaders(org.apache.http.Header[] apacheHeaders) {
    List<Header> headers = new ArrayList<Header>();

    for (org.apache.http.Header apacheHeader : apacheHeaders) {
      headers.add(new Header(apacheHeader.getName(), apacheHeader.getValue()));
    }

    return new Headers(headers);
  }

  private InputStream extractResponseStream(HttpResponse apacheResponse)
    throws IOException {
    HttpEntity entity = apacheResponse.getEntity();
    if (entity == null) {
      return EmptyInputStream.getInstance();
    } else {
      InputStream responseStream = entity.getContent();
      return responseStream == null ? EmptyInputStream.getInstance() : responseStream;
    }
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
