package com.hubspot.horizon;

import com.hubspot.horizon.HttpRequest.Options;
import java.io.Closeable;

public interface HttpClient extends Closeable {
  default HttpResponse execute(HttpRequest request) throws HttpRuntimeException {
    return execute(request, Options.DEFAULT);
  }

  HttpResponse execute(HttpRequest request, Options options) throws HttpRuntimeException;
}
