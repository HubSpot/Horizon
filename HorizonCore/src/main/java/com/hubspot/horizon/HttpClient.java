package com.hubspot.horizon;

import com.hubspot.horizon.HttpRequest.Options;
import java.io.Closeable;

public interface HttpClient extends Closeable {
  HttpResponse execute(HttpRequest request) throws HttpRuntimeException;
  HttpResponse execute(HttpRequest request, Options options) throws HttpRuntimeException;
}
