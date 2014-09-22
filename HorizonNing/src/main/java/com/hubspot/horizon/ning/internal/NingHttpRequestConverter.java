package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.Header;
import com.hubspot.horizon.HttpRequest;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

public final class NingHttpRequestConverter {

  private NingHttpRequestConverter() {
    throw new AssertionError();
  }

  public static Request convert(HttpRequest request) {
    RequestBuilder ningRequest = new RequestBuilder(request.getMethod().name());
    ningRequest.setURI(request.getUrl());

    if (request.getBody() != null && request.getMethod().allowsBody()) {
      ningRequest.setBody(request.getBody());
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
