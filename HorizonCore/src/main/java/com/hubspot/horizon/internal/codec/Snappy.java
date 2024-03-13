package com.hubspot.horizon.internal.codec;

import com.google.common.base.Optional;
import com.hubspot.horizon.Compression;
import java.io.IOException;

public enum Snappy implements Compression {
  INSTANCE;

  @Override
  public byte[] compress(byte[] data) {
    try {
      return org.xerial.snappy.Snappy.compress(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<String> getContentEncodingHeaderValue() {
    return Optional.of("snappy");
  }
}
