package com.hubspot.horizon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

public enum ObjectMapperHolder {
  INSTANCE;

  private final AtomicReference<ObjectMapper> mapper = new AtomicReference<ObjectMapper>(new ObjectMapper());

  public @Nonnull ObjectMapper get() {
    return mapper.get();
  }

  public void set(@Nonnull ObjectMapper mapper) {
    this.mapper.set(Preconditions.checkNotNull(mapper));
  }
}
