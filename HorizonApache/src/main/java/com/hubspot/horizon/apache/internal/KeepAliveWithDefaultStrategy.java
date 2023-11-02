package com.hubspot.horizon.apache.internal;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.protocol.HttpContext;

public class KeepAliveWithDefaultStrategy extends DefaultConnectionKeepAliveStrategy {

  private final long defaultValue;

  public KeepAliveWithDefaultStrategy(long defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
    long headerValue = super.getKeepAliveDuration(response, context);

    return headerValue < 0 ? defaultValue : headerValue;
  }
}
