package com.hubspot.horizon.ning.internal;

import com.google.common.net.HttpHeaders;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.RequestFilter;

public class AcceptEncodingRequestFilter implements RequestFilter {

  @Override
  public FilterContext filter(FilterContext context) {
    if (!context.getRequest().getHeaders().containsKey(HttpHeaders.ACCEPT_ENCODING)) {
      context.getRequest().getHeaders().add(HttpHeaders.ACCEPT_ENCODING, "snappy,gzip,deflate");
    }
    return context;
  }
}
