package com.hubspot.horizon.apache.internal;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

@NotThreadSafe
public class GenericHttpRequestWithBody extends HttpEntityEnclosingRequestBase {
  private final String method;

  public GenericHttpRequestWithBody(final URI uri, final String method) {
    super();
    setURI(uri);
    this.method = method;
  }

  public String getMethod() {
    return method;
  }
}
