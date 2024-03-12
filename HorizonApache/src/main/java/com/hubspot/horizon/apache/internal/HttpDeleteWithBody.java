package com.hubspot.horizon.apache.internal;

import java.net.URI;
import org.apache.http.annotation.Contract;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

@Contract(threading = org.apache.http.annotation.ThreadingBehavior.UNSAFE)
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

  public String getMethod() {
    return HttpDelete.METHOD_NAME;
  }

  public HttpDeleteWithBody(final String uri) {
    super();
    setURI(URI.create(uri));
  }

  public HttpDeleteWithBody(final URI uri) {
    super();
    setURI(uri);
  }

  public HttpDeleteWithBody() {
    super();
  }
}
