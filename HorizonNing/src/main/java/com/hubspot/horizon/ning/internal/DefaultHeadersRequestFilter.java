package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.HorizonHeaders;
import com.hubspot.horizon.HttpConfig;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.RequestFilter;

public class DefaultHeadersRequestFilter implements RequestFilter {

  private HttpConfig config;

  public DefaultHeadersRequestFilter(HttpConfig config) {
    this.config = config;
  }

  @Override
  public <T> FilterContext<T> filter(FilterContext<T> context) {
    HttpHeaders headers = context.getRequest().getHeaders();
    if (!headers.contains(HorizonHeaders.REQUEST_TIMEOUT)) {
      headers.add(
        HorizonHeaders.REQUEST_TIMEOUT,
        String.valueOf(
          context.getRequest().getRequestTimeout().isZero()
            ? config.getRequestTimeoutMillis()
            : context.getRequest().getRequestTimeout()
        )
      );
    }
    return context;
  }
}
