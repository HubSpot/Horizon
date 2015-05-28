package com.hubspot.horizon.internal.codec;

import com.google.common.base.Optional;
import com.hubspot.horizon.Compression;

public enum None implements Compression {
  INSTANCE;

  @Override
  public byte[] compress(byte[] data) {
    return data;
  }

  @Override
  public Optional<String> getContentEncodingHeaderValue() {
    return Optional.absent();
  }
}
