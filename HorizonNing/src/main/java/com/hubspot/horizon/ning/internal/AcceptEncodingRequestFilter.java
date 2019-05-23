package com.hubspot.horizon.ning.internal;

import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.RequestFilter;

import com.google.common.net.HttpHeaders;

public class AcceptEncodingRequestFilter implements RequestFilter {

  @Override
  public <T> FilterContext<T> filter(FilterContext<T> context) {
    if (!context.getRequest().getHeaders().contains(HttpHeaders.ACCEPT_ENCODING)) {
      context.getRequest().getHeaders().add(HttpHeaders.ACCEPT_ENCODING, "snappy,gzip,deflate");
    }

    return context;
  }
}
