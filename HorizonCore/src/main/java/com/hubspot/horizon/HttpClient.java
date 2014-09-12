package com.hubspot.horizon;

import com.hubspot.horizon.HttpRequest.Options;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Closeable;

@ParametersAreNonnullByDefault
public interface HttpClient extends Closeable {
  @Nonnull HttpResponse execute(HttpRequest request) throws HttpRuntimeException;
  @Nonnull HttpResponse execute(HttpRequest request, Options options) throws HttpRuntimeException;
}
