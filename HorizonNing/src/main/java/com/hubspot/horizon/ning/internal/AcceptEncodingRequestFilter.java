package com.hubspot.horizon.ning.internal;

import com.google.common.net.HttpHeaders;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.RequestFilter;

public class AcceptEncodingRequestFilter implements RequestFilter {

  @Override
  public FilterContext filter(FilterContext context) {
    context.getRequest().getHeaders().add(HttpHeaders.CONTENT_ENCODING, "snappy,gzip,deflate");
    return context;
  }
}
