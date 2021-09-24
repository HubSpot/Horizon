package com.hubspot.horizon.apache.internal;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import com.google.common.net.HttpHeaders;
import com.hubspot.horizon.HorizonHeaders;
import com.hubspot.horizon.HttpConfig;

public class DefaultHeadersRequestInterceptor implements HttpRequestInterceptor {
  private final HttpConfig config;

  public DefaultHeadersRequestInterceptor(HttpConfig config) {
    this.config = config;
  }

  @Override
  public void process(HttpRequest request, HttpContext context) {
    if (!request.containsHeader(HttpHeaders.ACCEPT_ENCODING)) {
      request.addHeader(HttpHeaders.ACCEPT_ENCODING, "snappy,gzip,deflate");
    }
    if (!request.containsHeader(HttpHeaders.USER_AGENT)) {
      request.addHeader(HttpHeaders.USER_AGENT, config.getUserAgent());
    }
    if (!request.containsHeader(HorizonHeaders.REQUEST_TIMEOUT)) {
      request.addHeader(HorizonHeaders.REQUEST_TIMEOUT, String.valueOf(config.getRequestTimeoutMillis()));
    }
  }
}
