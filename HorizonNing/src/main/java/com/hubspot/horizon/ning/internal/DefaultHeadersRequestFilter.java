package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.HorizonHeaders;
import com.hubspot.horizon.HttpConfig;
import org.asynchttpclient.shaded.filter.FilterContext;
import org.asynchttpclient.shaded.filter.RequestFilter;
import org.asynchttpclient.shaded.io.netty.handler.codec.http.HttpHeaders;

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
          context.getRequest().getRequestTimeout() > 0
            ? context.getRequest().getRequestTimeout()
            : config.getRequestTimeoutMillis()
        )
      );
    }
    return context;
  }
}
