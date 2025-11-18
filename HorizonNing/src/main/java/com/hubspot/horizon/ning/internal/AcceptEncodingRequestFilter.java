package com.hubspot.horizon.ning.internal;

import com.google.common.net.HttpHeaders;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.RequestFilter;

public class AcceptEncodingRequestFilter implements RequestFilter {

  @Override
  public <T> FilterContext<T> filter(FilterContext<T> context) {
    if (!context.getRequest().getHeaders().contains(HttpHeaders.ACCEPT_ENCODING)) {
      context
        .getRequest()
        .getHeaders()
        .add(HttpHeaders.ACCEPT_ENCODING, "snappy,gzip,deflate");
    }

    return context;
  }
}
