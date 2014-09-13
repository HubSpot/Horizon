package com.hubspot.horizon.internal.codec;

import com.google.common.base.Optional;
import com.hubspot.horizon.Codec;

import java.io.IOException;

public enum Snappy implements Codec {
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
  public byte[] decompress(byte[] data) {
    try {
      return org.xerial.snappy.Snappy.uncompress(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<String> getContentEncodingHeaderValue() {
    return Optional.of("snappy");
  }
}
