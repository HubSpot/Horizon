package com.hubspot.horizon;

import com.google.common.base.Preconditions;

public class HttpRuntimeException extends RuntimeException {

  public HttpRuntimeException(Throwable cause) {
    super(Preconditions.checkNotNull(cause));
  }
}
