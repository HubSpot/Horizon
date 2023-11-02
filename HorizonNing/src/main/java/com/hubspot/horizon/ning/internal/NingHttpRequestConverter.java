package com.hubspot.horizon.ning.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.common.net.HttpHeaders;
import com.hubspot.horizon.Header;
import com.hubspot.horizon.HttpRequest;
import java.util.Map;
import org.asynchttpclient.shaded.Request;
import org.asynchttpclient.shaded.RequestBuilder;
import org.asynchttpclient.shaded.io.netty.handler.codec.http.cookie.DefaultCookie;

public final class NingHttpRequestConverter {

  private static final MapSplitter COOKIE_SPLITTER = Splitter
    .on(";")
    .trimResults()
    .withKeyValueSeparator('=');

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

      if (HttpHeaders.HOST.equalsIgnoreCase(name)) {
        ningRequest.setVirtualHost(header.getValue());
      } else if (HttpHeaders.COOKIE.equalsIgnoreCase(name)) {
        Map<String, String> cookies = COOKIE_SPLITTER.split(header.getValue());

        /*
        need to use RequestBuilder#addCookie. simply adding a cookie header will
        get blown away if there was a set-cookie directive on a previous response
         */
        cookies.forEach((cookieName, value) -> {
          ningRequest.addCookie(new DefaultCookie(cookieName, value));
        });
      } else {
        ningRequest.addHeader(name, header.getValue());
      }
    }

    return ningRequest.build();
  }
}
