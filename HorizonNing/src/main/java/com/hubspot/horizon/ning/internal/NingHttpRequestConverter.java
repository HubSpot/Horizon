package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.HttpRequest;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map.Entry;

public final class NingHttpRequestConverter {

  private NingHttpRequestConverter() {
    throw new AssertionError();
  }

  public static @Nonnull Request convert(@Nonnull HttpRequest request) {
    RequestBuilder ningRequest = new RequestBuilder(request.getMethod().name());
    ningRequest.setURI(request.getUrl());

    if (request.getBody() != null && request.getMethod().allowsBody()) {
      ningRequest.setBody(request.getBody());
    }

    for (Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
      String name = entry.getKey();

      for (String value : entry.getValue()) {
        if ("Host".equalsIgnoreCase(name)) {
          ningRequest.setVirtualHost(value);
        } else {
          ningRequest.addHeader(name, value);
        }
      }
    }

    return ningRequest.build();
  }
}
