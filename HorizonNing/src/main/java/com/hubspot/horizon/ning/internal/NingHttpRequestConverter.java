package com.hubspot.horizon.ning.internal;

import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.horizon.Header;
import com.hubspot.horizon.HttpRequest;

public final class NingHttpRequestConverter {
  private final ObjectMapper mapper;

  public NingHttpRequestConverter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public Request convert(HttpRequest request) {
    RequestBuilder ningRequest = new RequestBuilder(request.getMethod().name());
    ningRequest.setUrl(request.getUrl().toString());

    byte[] body = request.getBody(mapper);
    if (body != null) {
      ningRequest.setBody(body);
    }

    for (Header header : request.getHeaders()) {
      String name = header.getName();

      if ("Host".equalsIgnoreCase(name)) {
        ningRequest.setVirtualHost(header.getValue());
      } else {
        ningRequest.addHeader(name, header.getValue());
      }
    }

    return ningRequest.build();
  }
}
