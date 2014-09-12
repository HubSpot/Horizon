package com.hubspot.horizon.apache.internal;

import com.google.common.net.HttpHeaders;
import com.hubspot.horizon.HttpConfig;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class DefaultHeadersRequestInterceptor implements HttpRequestInterceptor {
  private final HttpConfig config;

  public DefaultHeadersRequestInterceptor(HttpConfig config) {
    this.config = config;
  }

  @Override
  public void process(HttpRequest request, HttpContext context) {
    request.addHeader(HttpHeaders.ACCEPT_ENCODING, "snappy,gzip,deflate");
    request.addHeader(HttpHeaders.USER_AGENT, config.getUserAgent());
  }
}
