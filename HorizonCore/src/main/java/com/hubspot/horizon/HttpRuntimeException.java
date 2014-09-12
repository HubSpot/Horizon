package com.hubspot.horizon;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public class HttpRuntimeException extends RuntimeException {

  public HttpRuntimeException(@Nonnull Throwable cause) {
    super(Preconditions.checkNotNull(cause));
  }
}
